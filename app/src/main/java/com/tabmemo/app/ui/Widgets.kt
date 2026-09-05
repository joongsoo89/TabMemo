package com.tabmemo.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tabmemo.app.data.ImageStore
import com.tabmemo.app.data.MAIN_TAB_ID
import com.tabmemo.app.data.TabMemo
import com.tabmemo.app.ui.theme.Cream
import com.tabmemo.app.ui.theme.Muted
import com.tabmemo.app.ui.theme.Teal
import com.tabmemo.app.ui.theme.Warm
import java.io.File

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelRow(
    labels: List<String>,
    selected: String?,
    allLabel: String,
    onSelect: (String?) -> Unit,
    removable: Boolean = false,
    filterable: Boolean = true,
    onRemove: (String) -> Unit = {},
) {
    if (labels.isEmpty() && selected == null) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (filterable && !removable) {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(allLabel) },
                colors = labelChipColors(),
            )
        }
        labels.forEach { label ->
            FilterChip(
                selected = selected?.equals(label, ignoreCase = true) == true,
                onClick = {
                    when {
                        removable -> onRemove(label)
                        filterable -> onSelect(if (selected == label) null else label)
                    }
                },
                label = { Text(if (removable) "$label ×" else label) },
                colors = labelChipColors(),
            )
        }
    }
}

@Composable
private fun labelChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color(0xFFE4ECE7),
    selectedContainerColor = Teal,
    labelColor = Teal,
    selectedLabelColor = Cream,
)

@Composable
fun PhotoStrip(
    notebookId: String,
    imageIds: List<String>,
    imageStore: ImageStore,
    editable: Boolean,
    addLabel: String,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
) {
    var viewing by remember { mutableStateOf<File?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        imageIds.forEach { id ->
            val file = imageStore.file(notebookId, id)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4ECE7))
                    .clickable { viewing = file },
            ) {
                MemoPhoto(file = file, modifier = Modifier.fillMaxSize(), crop = true)
                if (editable) {
                    TextButton(
                        onClick = { onRemove(id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) { Text("×", color = Warm, fontSize = 18.sp) }
                }
            }
        }
        if (editable) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE4ECE7))
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                AddPhotoIcon(
                    modifier = Modifier.size(36.dp),
                    tint = Teal,
                    contentDescription = addLabel,
                )
            }
        }
    }
    viewing?.let { file ->
        PhotoViewerDialog(file = file, onDismiss = { viewing = null })
    }
}

@Composable
private fun AddPhotoIcon(
    modifier: Modifier = Modifier,
    tint: Color,
    contentDescription: String,
) {
    Canvas(modifier.semantics { this.contentDescription = contentDescription }) {
        val stroke = size.minDimension * 0.09f
        val body = Size(size.width * 0.78f, size.height * 0.56f)
        val topLeft = Offset((size.width - body.width) / 2f, size.height * 0.32f)
        drawRoundRect(
            color = tint,
            topLeft = topLeft,
            size = body,
            cornerRadius = CornerRadius(size.minDimension * 0.1f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = tint,
            radius = size.minDimension * 0.14f,
            center = Offset(size.width / 2f, topLeft.y + body.height / 2f),
            style = Stroke(width = stroke),
        )
        val bumpWidth = size.width * 0.22f
        val bumpHeight = size.height * 0.12f
        drawRoundRect(
            color = tint,
            topLeft = Offset((size.width - bumpWidth) / 2f, topLeft.y - bumpHeight + stroke),
            size = Size(bumpWidth, bumpHeight),
            cornerRadius = CornerRadius(stroke),
            style = Stroke(width = stroke),
        )
        val plus = size.minDimension * 0.16f
        val plusCenter = Offset(size.width * 0.82f, size.height * 0.2f)
        drawCircle(color = tint, radius = plus * 1.15f, center = plusCenter)
        drawLine(
            color = Cream,
            start = Offset(plusCenter.x - plus * 0.55f, plusCenter.y),
            end = Offset(plusCenter.x + plus * 0.55f, plusCenter.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Cream,
            start = Offset(plusCenter.x, plusCenter.y - plus * 0.55f),
            end = Offset(plusCenter.x, plusCenter.y + plus * 0.55f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun PhotoViewerDialog(file: File, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            ZoomablePhoto(file = file)
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000)),
            ) { Text("✕", color = Color.White, fontSize = 18.sp) }
        }
    }
}

@Composable
private fun ZoomablePhoto(file: File) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    MemoPhoto(
        file = file,
        crop = false,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val next = (scale * zoom).coerceIn(1f, 6f)
                    scale = next
                    offset = if (next <= 1.01f) Offset.Zero else offset + pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
    )
}

@Composable
fun MemoPhoto(file: File, modifier: Modifier = Modifier, crop: Boolean = true) {
    val bitmap = remember(file.path, file.lastModified()) {
        runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
        )
    }
}

@Composable
fun MadeByFooter(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, color = Muted, fontSize = 12.sp)
    }
}
