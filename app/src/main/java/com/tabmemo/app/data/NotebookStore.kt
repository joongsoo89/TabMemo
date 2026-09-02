package com.tabmemo.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class NotebookStore(context: Context) {
    private val file = File(context.filesDir, "notebooks.json")
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun load(): List<Notebook> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext emptyList()
            runCatching {
                json.decodeFromString<List<Notebook>>(file.readText(Charsets.UTF_8))
                    .sortedByDescending { it.updatedAt }
            }.getOrDefault(emptyList())
        }
    }

    suspend fun save(notebooks: List<Notebook>) = mutex.withLock {
        withContext(Dispatchers.IO) {
            file.writeText(json.encodeToString(notebooks), Charsets.UTF_8)
        }
    }
}
