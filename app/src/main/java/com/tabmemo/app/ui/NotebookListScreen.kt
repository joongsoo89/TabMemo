package com.tabmemo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.matches
import com.tabmemo.app.data.snippet
import com.tabmemo.app.ui.theme.Cream
import com.tabmemo.app.ui.theme.Ink
import com.tabmemo.app.ui.theme.Line
import com.tabmemo.app.ui.theme.Muted
import com.tabmemo.app.ui.theme.Paper
import com.tabmemo.app.ui.theme.Teal
import com.tabmemo.app.ui.theme.TealSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookListScreen(
    lang: Lang,
    notebooks: List<Notebook>,
    onLang: (Lang) -> Unit,
    onOpen: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val t = stringsForLang(lang)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = notebooks.filter { it.matches(query) }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text(t.appName) },
                actions = { LanguageBar(lang, onLang) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper, titleContentColor = Ink),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = Teal, contentColor = Cream) {
                Text("+ ${t.add}", modifier = Modifier.padding(horizontal = 8.dp))
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(t.search) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Cream,
                    unfocusedContainerColor = Cream,
                    focusedBorderColor = Line,
                    unfocusedBorderColor = Line,
                ),
            )
            Spacer(Modifier.height(12.dp))
            if (notebooks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(88.dp)
                                .background(TealSoft, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("📒", fontSize = 36.sp) }
                        Spacer(Modifier.height(12.dp))
                        Text(t.emptyTitle, color = Ink, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(t.emptyHint, color = Muted)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.id }) { notebook ->
                        NotebookCard(lang, notebook, onClick = { onOpen(notebook.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NotebookCard(lang: Lang, notebook: Notebook, onClick: () -> Unit) {
    val t = stringsForLang(lang)
    val snippet = notebook.snippet()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color(0xFFE7F0EA), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) { Text(notebook.emoji, fontSize = 26.sp) }
            Column(Modifier.weight(1f)) {
                Text(
                    notebook.title.ifBlank { t.titleHint },
                    color = Ink,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(t.tabCount(notebook.tabs.size), color = Muted, fontSize = 13.sp)
                if (snippet.isNotEmpty()) {
                    Text(snippet, color = Muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
