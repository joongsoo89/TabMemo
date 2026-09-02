package com.tabmemo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabmemo.app.data.MAIN_TAB_ID
import com.tabmemo.app.data.TabMemo
import com.tabmemo.app.ui.theme.Cream
import com.tabmemo.app.ui.theme.Muted
import com.tabmemo.app.ui.theme.Teal

@Composable
fun LanguageBar(lang: Lang, onChange: (Lang) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE4ECE7))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(Lang.Ko to "KO", Lang.Ja to "JP", Lang.En to "EN").forEach { (value, label) ->
            val selected = value == lang
            Button(
                onClick = { onChange(value) },
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) Cream else Color.Transparent,
                    contentColor = if (selected) Teal else Muted,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(label, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MemoTabBar(
    lang: Lang,
    selectedId: String,
    tabs: List<TabMemo>,
    showAdd: Boolean,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val t = stringsForLang(lang)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE4ECE7))
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabChip(
            label = t.mainMemoShort,
            selected = selectedId == MAIN_TAB_ID,
            onClick = { onSelect(MAIN_TAB_ID) },
        )
        tabs.forEach { tab ->
            TabChip(
                label = tab.title.ifBlank { t.newTabTitle },
                selected = selectedId == tab.id,
                onClick = { onSelect(tab.id) },
            )
        }
        if (showAdd) {
            TabChip(
                label = t.addTab,
                selected = false,
                onClick = onAdd,
            )
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(42.dp)
            .widthIn(min = 48.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Cream else Color.Transparent,
            contentColor = if (selected) Teal else Muted,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp),
    ) {
        Text(label, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
fun AddTabDialog(
    lang: Lang,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val t = stringsForLang(lang)
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t.newTabTitle) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(t.newTabHint) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val title = name.trim()
                    if (title.isNotEmpty()) onConfirm(title)
                },
            ) { Text(t.addAction) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t.cancel) }
        },
    )
}
