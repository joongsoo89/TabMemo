package com.tabmemo.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

const val MAIN_TAB_ID = "main"

@Serializable
data class TabMemo(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
data class Notebook(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String = randomEmoji(),
    val title: String = "",
    val body: String = "",
    val labels: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val tabs: List<TabMemo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

fun randomEmoji(): String = EMOJIS.random()

private val EMOJIS = listOf(
    "📝", "📒", "📌", "💡", "⭐", "🏠", "✈️", "🛒",
    "💼", "🎓", "💪", "🌸", "🍳", "🎵", "📚", "🗓️",
)

fun Notebook.snippet(): String {
    val main = body.trim()
    if (main.isNotEmpty()) return main
    for (tab in tabs) {
        val text = tab.body.trim().ifEmpty { tab.title.trim() }
        if (text.isNotEmpty()) return text
    }
    return ""
}

fun Notebook.tabText(tabId: String): String =
    if (tabId == MAIN_TAB_ID) body else tabs.find { it.id == tabId }?.body.orEmpty()

fun Notebook.tabTitle(tabId: String): String =
    if (tabId == MAIN_TAB_ID) "" else tabs.find { it.id == tabId }?.title.orEmpty()

fun Notebook.hasTab(tabId: String): Boolean =
    tabId == MAIN_TAB_ID || tabs.any { it.id == tabId }

fun Notebook.imagesOf(tabId: String): List<String> =
    if (tabId == MAIN_TAB_ID) images else tabs.find { it.id == tabId }?.images.orEmpty()

fun Notebook.allImageIds(): List<String> =
    images + tabs.flatMap { it.images }

fun Notebook.withBody(value: String): Notebook =
    copy(body = value, updatedAt = System.currentTimeMillis())

fun Notebook.withTabText(tabId: String, value: String): Notebook {
    if (tabId == MAIN_TAB_ID) return withBody(value)
    return copy(
        tabs = tabs.map { if (it.id == tabId) it.copy(body = value, updatedAt = System.currentTimeMillis()) else it },
        updatedAt = System.currentTimeMillis(),
    )
}

fun Notebook.withTabTitle(tabId: String, title: String): Notebook = copy(
    tabs = tabs.map { if (it.id == tabId) it.copy(title = title, updatedAt = System.currentTimeMillis()) else it },
    updatedAt = System.currentTimeMillis(),
)

fun Notebook.withImages(tabId: String, imageIds: List<String>): Notebook {
    return if (tabId == MAIN_TAB_ID) {
        copy(images = imageIds, updatedAt = System.currentTimeMillis())
    } else {
        copy(
            tabs = tabs.map { if (it.id == tabId) it.copy(images = imageIds, updatedAt = System.currentTimeMillis()) else it },
            updatedAt = System.currentTimeMillis(),
        )
    }
}

fun Notebook.addImage(tabId: String, imageId: String): Notebook =
    withImages(tabId, imagesOf(tabId) + imageId)

fun Notebook.removeImage(tabId: String, imageId: String): Notebook =
    withImages(tabId, imagesOf(tabId).filterNot { it == imageId })

fun Notebook.addTab(title: String): Pair<Notebook, TabMemo> {
    val tab = TabMemo(title = title.trim())
    return copy(
        tabs = tabs + tab,
        updatedAt = System.currentTimeMillis(),
    ) to tab
}

fun Notebook.removeTab(tabId: String): Notebook = copy(
    tabs = tabs.filterNot { it.id == tabId },
    updatedAt = System.currentTimeMillis(),
)

fun Notebook.normalizedLabels(): List<String> =
    labels.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

fun Notebook.addLabel(label: String): Notebook {
    val next = label.trim()
    if (next.isEmpty()) return this
    val current = normalizedLabels()
    if (current.any { it.equals(next, ignoreCase = true) }) return this
    return copy(labels = current + next, updatedAt = System.currentTimeMillis())
}

fun Notebook.removeLabel(label: String): Notebook = copy(
    labels = normalizedLabels().filterNot { it.equals(label, ignoreCase = true) },
    updatedAt = System.currentTimeMillis(),
)

fun Notebook.matches(query: String, label: String? = null): Boolean {
    if (!label.isNullOrBlank() && normalizedLabels().none { it.equals(label, ignoreCase = true) }) {
        return false
    }
    if (query.isBlank()) return true
    val hay = buildString {
        append(title)
        append(' ')
        append(body)
        normalizedLabels().forEach { item ->
            append(' ')
            append(item)
        }
        tabs.forEach { tab ->
            append(' ')
            append(tab.title)
            append(' ')
            append(tab.body)
        }
    }
    return hay.contains(query, ignoreCase = true)
}

fun Notebook.fileName(): String {
    val base = title.trim().ifBlank { "tabmemo" }
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .take(40)
    return "$base.tabmemo"
}
