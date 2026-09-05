package com.tabmemo.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tabmemo.app.data.ImageStore
import com.tabmemo.app.data.MemoCodec
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.NotebookStore
import com.tabmemo.app.data.allImageIds
import com.tabmemo.app.ui.Lang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = NotebookStore(application)
    val images = ImageStore(application)
    private val prefs = application.getSharedPreferences("tabmemo", 0)

    private val _notebooks = MutableStateFlow<List<Notebook>>(emptyList())
    val notebooks: StateFlow<List<Notebook>> = _notebooks.asStateFlow()

    private val _lang = MutableStateFlow(readLang())
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _notebooks.value = store.load()
        }
    }

    fun setLang(lang: Lang) {
        _lang.value = lang
        prefs.edit().putString("lang", lang.name).apply()
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun upsert(notebook: Notebook) {
        val next = notebook.copy(updatedAt = System.currentTimeMillis())
        _notebooks.update { current ->
            val without = current.filterNot { it.id == next.id }
            listOf(next) + without
        }
        persist()
    }

    fun delete(id: String) {
        _notebooks.update { it.filterNot { notebook -> notebook.id == id } }
        images.deleteNotebook(id)
        persist()
    }

    fun notebook(id: String?): Notebook? = _notebooks.value.find { it.id == id }

    fun notify(code: String) {
        _message.value = code
    }

    fun exportText(notebook: Notebook): String =
        MemoCodec.encode(notebook, images.exportMap(notebook))

    fun importText(text: String, targetId: String? = null): Notebook? {
        return runCatching {
            val parsed = MemoCodec.decode(text)
            val incoming = parsed.notebook
            val existing = targetId?.let { id -> _notebooks.value.find { it.id == id } }
            val merged = if (existing != null) {
                incoming.copy(
                    id = existing.id,
                    createdAt = existing.createdAt,
                    emoji = incoming.emoji.ifBlank { existing.emoji },
                    title = incoming.title.ifBlank { existing.title },
                )
            } else {
                val byId = _notebooks.value.find { it.id == incoming.id }
                if (byId != null) incoming.copy(createdAt = byId.createdAt) else incoming
            }
            if (merged.title.isBlank()) {
                _message.value = "empty-title"
                return null
            }
            images.importMap(merged.id, parsed.images)
            upsert(merged)
            _message.value = "import-ok"
            merged
        }.getOrElse {
            _message.value = "import-fail"
            null
        }
    }

    private fun persist() {
        val snapshot = _notebooks.value
        val keep = snapshot.associate { it.id to it.allImageIds().toSet() }
        viewModelScope.launch {
            store.save(snapshot)
            images.gc(keep)
        }
    }

    private fun readLang(): Lang {
        val stored = prefs.getString("lang", null)
        return Lang.entries.find { it.name == stored } ?: Lang.fromSystem()
    }
}
