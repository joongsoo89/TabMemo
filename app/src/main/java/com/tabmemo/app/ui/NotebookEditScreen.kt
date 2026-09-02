package com.tabmemo.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.tabmemo.app.data.MAIN_TAB_ID
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.addTab
import com.tabmemo.app.data.hasTab
import com.tabmemo.app.data.randomEmoji
import com.tabmemo.app.data.removeTab
import com.tabmemo.app.data.tabText
import com.tabmemo.app.data.tabTitle
import com.tabmemo.app.data.withTabText
import com.tabmemo.app.data.withTabTitle
import com.tabmemo.app.ui.theme.Cream
import com.tabmemo.app.ui.theme.Ink
import com.tabmemo.app.ui.theme.Line
import com.tabmemo.app.ui.theme.Muted
import com.tabmemo.app.ui.theme.Paper
import com.tabmemo.app.ui.theme.Teal
import com.tabmemo.app.ui.theme.Warm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookEditScreen(
    lang: Lang,
    initial: Notebook,
    onSave: (Notebook) -> Unit,
    onCancel: () -> Unit,
    onLang: (Lang) -> Unit,
    onNeedTitle: () -> Unit,
    onNeedTabTitle: () -> Unit,
) {
    val t = stringsForLang(lang)
    var notebook by remember(initial.id) { mutableStateOf(initial) }
    var tabId by rememberSaveable { mutableStateOf(MAIN_TAB_ID) }
    var bodyFocused by remember { mutableStateOf(false) }
    var showAddTab by remember { mutableStateOf(false) }
    var confirmDeleteTab by remember { mutableStateOf(false) }
    val selected = if (notebook.hasTab(tabId)) tabId else MAIN_TAB_ID
    val isCustom = selected != MAIN_TAB_ID
    val heading = if (isCustom) notebook.tabTitle(selected) else t.mainMemo
    val placeholder = if (isCustom) t.tabPlaceholder else t.mainPlaceholder

    Scaffold(
        containerColor = Paper,
        topBar = {
            if (!bodyFocused) {
                TopAppBar(
                    title = { Text(if (initial.title.isBlank()) t.add else t.edit) },
                    navigationIcon = { TextButton(onClick = onCancel) { Text("←") } },
                    actions = { LanguageBar(lang, onLang) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Paper,
                        titleContentColor = Ink,
                    ),
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(top = if (bodyFocused) 8.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!bodyFocused) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { notebook = notebook.copy(emoji = randomEmoji()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cream,
                            contentColor = Ink,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text(notebook.emoji) }
                    Field(
                        label = t.title,
                        value = notebook.title,
                        hint = t.titleHint,
                        modifier = Modifier.weight(1f),
                        onChange = { notebook = notebook.copy(title = it) },
                    )
                }
            }

            MemoTabBar(
                lang = lang,
                selectedId = selected,
                tabs = notebook.tabs,
                showAdd = true,
                onSelect = { tabId = it },
                onAdd = { showAddTab = true },
            )

            if (isCustom) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = heading,
                        onValueChange = { notebook = notebook.withTabTitle(selected, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(t.newTabTitle) },
                        colors = fieldColors(),
                    )
                    TextButton(onClick = { confirmDeleteTab = true }) {
                        Text(t.deleteTab, color = Warm)
                    }
                }
            } else {
                Text(t.mainMemo, color = Ink)
            }

            OutlinedTextField(
                value = notebook.tabText(selected),
                onValueChange = { notebook = notebook.withTabText(selected, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onFocusChanged { bodyFocused = it.isFocused },
                placeholder = { Text(placeholder) },
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Button(
                    onClick = {
                        when {
                            notebook.title.isBlank() -> onNeedTitle()
                            notebook.tabs.any { it.title.isBlank() } -> onNeedTabTitle()
                            else -> onSave(notebook.copy(updatedAt = System.currentTimeMillis()))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                ) { Text(t.save) }
                TextButton(onClick = onCancel) { Text(t.cancel, color = Warm) }
            }
        }
    }

    if (showAddTab) {
        AddTabDialog(
            lang = lang,
            onConfirm = { title ->
                val (next, tab) = notebook.addTab(title)
                notebook = next
                tabId = tab.id
                showAddTab = false
            },
            onDismiss = { showAddTab = false },
        )
    }
    if (confirmDeleteTab && isCustom) {
        AlertDialog(
            onDismissRequest = { confirmDeleteTab = false },
            title = { Text(t.deleteTab) },
            text = { Text(t.confirmDeleteTab) },
            confirmButton = {
                TextButton(onClick = {
                    notebook = notebook.removeTab(selected)
                    tabId = MAIN_TAB_ID
                    confirmDeleteTab = false
                }) { Text(t.delete, color = Warm) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTab = false }) { Text(t.cancel) }
            },
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Muted)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(hint) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Cream,
    unfocusedContainerColor = Cream,
    focusedBorderColor = Line,
    unfocusedBorderColor = Line,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
)
