package com.tabmemo.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tabmemo.app.data.MAIN_TAB_ID
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.fileName
import com.tabmemo.app.ui.NotebookDetailScreen
import com.tabmemo.app.ui.NotebookEditScreen
import com.tabmemo.app.ui.NotebookListScreen
import com.tabmemo.app.ui.stringsForLang
import com.tabmemo.app.ui.theme.TabMemoTheme
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()
    private var pendingViewUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingViewUri = intent.data.takeIf { intent.action == Intent.ACTION_VIEW }
        setContent {
            TabMemoTheme {
                TabMemoApp(viewModel, pendingViewUri) { pendingViewUri = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            pendingViewUri = intent.data
        }
    }
}

@Composable
private fun TabMemoApp(
    viewModel: AppViewModel,
    incomingUri: Uri?,
    onIncomingConsumed: () -> Unit,
) {
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val t = stringsForLang(lang)
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var exportPayload by remember { mutableStateOf<Pair<String, String>?>(null) }
    var importTargetId by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val payload = exportPayload
        exportPayload = null
        if (uri == null || payload == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.second.toByteArray(Charset.forName("UTF-8")))
            } ?: error("no stream")
            viewModel.notify("export-ok")
        }.onFailure { viewModel.notify("import-fail") }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = readUri(context, uri)
        if (text == null) {
            viewModel.notify("import-fail")
            return@rememberLauncherForActivityResult
        }
        val imported = viewModel.importText(text, importTargetId)
        importTargetId = null
        if (imported != null) {
            nav.navigate("detail/${imported.id}/$MAIN_TAB_ID")
        }
    }

    fun startExport(notebook: Notebook) {
        exportPayload = notebook.fileName() to viewModel.exportText(notebook)
        exportLauncher.launch(notebook.fileName())
    }

    fun startImport(targetId: String?) {
        importTargetId = targetId
        importLauncher.launch(arrayOf("application/json", "text/plain", "text/*", "*/*"))
    }

    LaunchedEffect(incomingUri) {
        val uri = incomingUri ?: return@LaunchedEffect
        val text = readUri(context, uri)
        onIncomingConsumed()
        if (text == null) {
            viewModel.notify("import-fail")
        } else {
            viewModel.importText(text)?.let { nav.navigate("detail/${it.id}/$MAIN_TAB_ID") }
        }
    }

    LaunchedEffect(message) {
        val code = message ?: return@LaunchedEffect
        val text = when (code) {
            "empty-title" -> t.needTitle
            "empty-tab" -> t.needTabTitle
            "saved" -> t.saved
            "copied" -> t.copied
            "import-fail" -> t.importFail
            "import-ok" -> t.importOk
            "export-ok" -> t.exportOk
            else -> null
        }
        viewModel.consumeMessage()
        if (text != null) snackbar.showSnackbar(text)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "list",
            modifier = Modifier.padding(padding),
        ) {
            composable("list") {
                NotebookListScreen(
                    lang = lang,
                    notebooks = notebooks,
                    onLang = viewModel::setLang,
                    onOpen = { nav.navigate("detail/$it/$MAIN_TAB_ID") },
                    onAdd = { nav.navigate("new") },
                    onImport = { startImport(null) },
                )
            }
            composable(
                "detail/{id}/{tabId}",
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("tabId") { type = NavType.StringType },
                ),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val tabId = entry.arguments?.getString("tabId") ?: MAIN_TAB_ID
                val notebook = viewModel.notebook(id)
                if (notebook == null) {
                    LaunchedEffect(id) { nav.goToList() }
                } else {
                    NotebookDetailScreen(
                        lang = lang,
                        notebook = notebook,
                        initialTabId = tabId,
                        imageStore = viewModel.images,
                        onBack = { nav.popBackStack() },
                        onEdit = { tabId -> nav.navigate("edit/${notebook.id}/$tabId") },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            clipboard.setPrimaryClip(ClipData.newPlainText("TabMemo", text))
                            viewModel.notify("copied")
                        },
                        onDelete = {
                            nav.goToList()
                            viewModel.delete(notebook.id)
                        },
                        onExport = { startExport(notebook) },
                        onImport = { startImport(notebook.id) },
                        onLang = viewModel::setLang,
                    )
                }
            }
            composable("new") {
                NotebookEditScreen(
                    lang = lang,
                    initial = remember { Notebook() },
                    imageStore = viewModel.images,
                    onSave = { notebook, tabId ->
                        viewModel.upsert(notebook)
                        viewModel.notify("saved")
                        nav.navigate("detail/${notebook.id}/$tabId") {
                            popUpTo("list") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onCancel = { nav.popBackStack() },
                    onLang = viewModel::setLang,
                    onNeedTitle = { viewModel.notify("empty-title") },
                    onNeedTabTitle = { viewModel.notify("empty-tab") },
                )
            }
            composable(
                "edit/{id}/{tabId}",
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("tabId") { type = NavType.StringType },
                ),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val tabId = entry.arguments?.getString("tabId") ?: MAIN_TAB_ID
                val notebook = viewModel.notebook(id)
                if (notebook == null) {
                    LaunchedEffect(id) { nav.goToList() }
                } else {
                    NotebookEditScreen(
                        lang = lang,
                        initial = notebook,
                        initialTabId = tabId,
                        imageStore = viewModel.images,
                        onSave = { updated, tabId ->
                            viewModel.upsert(updated)
                            viewModel.notify("saved")
                            nav.navigate("detail/${updated.id}/$tabId") {
                                popUpTo("list") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onCancel = { nav.popBackStack() },
                        onLang = viewModel::setLang,
                        onNeedTitle = { viewModel.notify("empty-title") },
                        onNeedTabTitle = { viewModel.notify("empty-tab") },
                    )
                }
            }
        }
    }
}

private fun NavController.goToList() {
    navigate("list") {
        launchSingleTop = true
        popUpTo(graph.id) { inclusive = true }
    }
}

private fun readUri(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.reader(Charset.forName("UTF-8")).readText()
        }
    }.getOrNull()
}
