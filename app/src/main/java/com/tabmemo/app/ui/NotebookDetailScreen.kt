package com.tabmemo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabmemo.app.data.ImageStore
import com.tabmemo.app.data.MAIN_TAB_ID
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.hasTab
import com.tabmemo.app.data.imagesOf
import com.tabmemo.app.data.normalizedLabels
import com.tabmemo.app.data.tabText
import com.tabmemo.app.data.tabTitle
import com.tabmemo.app.ui.theme.Cream
import com.tabmemo.app.ui.theme.Ink
import com.tabmemo.app.ui.theme.Muted
import com.tabmemo.app.ui.theme.Paper
import com.tabmemo.app.ui.theme.Teal
import com.tabmemo.app.ui.theme.Warm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookDetailScreen(
    lang: Lang,
    notebook: Notebook,
    imageStore: ImageStore,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onCopy: (String) -> Unit,
    onLang: (Lang) -> Unit,
    initialTabId: String = MAIN_TAB_ID,
) {
    val t = stringsForLang(lang)
    var tabId by rememberSaveable(initialTabId) { mutableStateOf(initialTabId) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val selected = if (notebook.hasTab(tabId)) tabId else MAIN_TAB_ID
    val heading = if (selected == MAIN_TAB_ID) t.mainMemo else notebook.tabTitle(selected).ifBlank { t.newTabTitle }
    val rawBody = notebook.tabText(selected)
    val body = rawBody.ifBlank { t.noContent }
    val photos = notebook.imagesOf(selected)
    val labels = notebook.normalizedLabels()
    val context = LocalContext.current

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text(notebook.title.ifBlank { t.appName }, maxLines = 1) },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
                actions = { LanguageBar(lang, onLang) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper, titleContentColor = Ink),
            )
        },
    ) { padding ->
        val memoScroll = rememberScrollState()
        LaunchedEffect(selected) { memoScroll.scrollTo(0) }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .background(Color(0xFFE7F0EA), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text(notebook.emoji, fontSize = 26.sp) }
                        Column {
                            Text(notebook.title, color = Ink, fontSize = 20.sp)
                            Text(t.tabCount(notebook.tabs.size), color = Muted)
                        }
                    }
                    if (labels.isNotEmpty()) {
                        LabelRow(
                            labels = labels,
                            selected = null,
                            allLabel = t.allLabels,
                            onSelect = {},
                            filterable = false,
                        )
                    }
                    MemoTabBar(
                        lang = lang,
                        selectedId = selected,
                        tabs = notebook.tabs,
                        showAdd = false,
                        onSelect = { tabId = it },
                        onAdd = {},
                    )
                }
            }
            Text(heading, color = Ink, fontSize = 22.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = Cream),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(memoScroll)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SelectableLinkText(text = body)
                    if (photos.isNotEmpty()) {
                        Text(t.photos, color = Ink)
                        PhotoStrip(
                            notebookId = notebook.id,
                            imageIds = photos,
                            imageStore = imageStore,
                            editable = false,
                            addLabel = t.addPhoto,
                            onAdd = {},
                            onRemove = {},
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onEdit(selected) },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    ) {
                        Text(t.edit)
                    }
                    TextButton(
                        onClick = {
                            shareMemoText(
                                context = context,
                                title = notebook.title,
                                heading = heading,
                                body = rawBody,
                                chooserTitle = t.share,
                            )
                        },
                    ) { Text(t.share, color = Teal) }
                    TextButton(onClick = { onCopy(rawBody.ifBlank { "${notebook.title}\n$heading" }) }) {
                        Text(t.copy, color = Teal)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onExport) { Text(t.exportFile, color = Teal) }
                    TextButton(onClick = onImport) { Text(t.importIntoMemo, color = Teal) }
                    TextButton(onClick = { confirmDelete = true }) { Text(t.delete, color = Warm) }
                }
                MadeByFooter(t.madeBy)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(t.delete) },
            text = { Text(t.confirmDelete) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(t.delete, color = Warm) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(t.cancel) }
            },
        )
    }
}
