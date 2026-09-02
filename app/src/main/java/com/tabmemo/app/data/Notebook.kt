package com.tabmemo.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

const val MAIN_TAB_ID = "main"

@Serializable
data class TabMemo(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Serializable
data class Notebook(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String = randomEmoji(),
    val title: String = "",
    val body: String = "",
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

fun Notebook.matches(query: String): Boolean {
    if (query.isBlank()) return true
    val hay = buildString {
        append(title)
        append(' ')
        append(body)
        tabs.forEach { tab ->
            append(' ')
            append(tab.title)
            append(' ')
            append(tab.body)
        }
    }
    return hay.contains(query, ignoreCase = true)
}
