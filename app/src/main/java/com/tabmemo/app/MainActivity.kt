package com.tabmemo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.ui.NotebookDetailScreen
import com.tabmemo.app.ui.NotebookEditScreen
import com.tabmemo.app.ui.NotebookListScreen
import com.tabmemo.app.ui.stringsForLang
import com.tabmemo.app.ui.theme.TabMemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabMemoTheme {
                TabMemoApp(viewModel)
            }
        }
    }
}

@Composable
private fun TabMemoApp(viewModel: AppViewModel) {
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val t = stringsForLang(lang)
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val code = message ?: return@LaunchedEffect
        val text = when (code) {
            "empty-title" -> t.needTitle
            "empty-tab" -> t.needTabTitle
            "saved" -> t.saved
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
                    onOpen = { nav.navigate("detail/$it") },
                    onAdd = { nav.navigate("new") },
                )
            }
            composable(
                "detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val notebook = viewModel.notebook(id)
                if (notebook == null) {
                    LaunchedEffect(id) { nav.popBackStack() }
                } else {
                    NotebookDetailScreen(
                        lang = lang,
                        notebook = notebook,
                        onBack = { nav.popBackStack() },
                        onEdit = { nav.navigate("edit/${notebook.id}") },
                        onDelete = {
                            viewModel.delete(notebook.id)
                            nav.popBackStack()
                        },
                        onLang = viewModel::setLang,
                        onUpdate = viewModel::upsert,
                    )
                }
            }
            composable("new") {
                NotebookEditScreen(
                    lang = lang,
                    initial = remember { Notebook() },
                    onSave = { notebook ->
                        viewModel.upsert(notebook)
                        viewModel.notify("saved")
                        nav.navigate("detail/${notebook.id}") {
                            popUpTo("list")
                        }
                    },
                    onCancel = { nav.popBackStack() },
                    onLang = viewModel::setLang,
                    onNeedTitle = { viewModel.notify("empty-title") },
                    onNeedTabTitle = { viewModel.notify("empty-tab") },
                )
            }
            composable(
                "edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val notebook = viewModel.notebook(id)
                if (notebook == null) {
                    LaunchedEffect(id) { nav.popBackStack() }
                } else {
                    NotebookEditScreen(
                        lang = lang,
                        initial = notebook,
                        onSave = { updated ->
                            viewModel.upsert(updated)
                            viewModel.notify("saved")
                            nav.popBackStack()
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
