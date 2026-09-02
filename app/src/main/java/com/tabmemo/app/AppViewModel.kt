package com.tabmemo.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tabmemo.app.data.Notebook
import com.tabmemo.app.data.NotebookStore
import com.tabmemo.app.ui.Lang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = NotebookStore(application)
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
        persist()
    }

    fun notebook(id: String?): Notebook? = _notebooks.value.find { it.id == id }

    fun notify(code: String) {
        _message.value = code
    }

    private fun persist() {
        val snapshot = _notebooks.value
        viewModelScope.launch { store.save(snapshot) }
    }

    private fun readLang(): Lang {
        val stored = prefs.getString("lang", null)
        return Lang.entries.find { it.name == stored } ?: Lang.fromSystem()
    }
}
