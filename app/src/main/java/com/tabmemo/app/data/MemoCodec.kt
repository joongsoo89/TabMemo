package com.tabmemo.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MemoFile(
    val version: Int = 1,
    val notebook: Notebook,
    val images: Map<String, String> = emptyMap(),
)

object MemoCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun encode(notebook: Notebook, images: Map<String, String>): String {
        return json.encodeToString(MemoFile(notebook = notebook, images = images))
    }

    fun decode(raw: String): MemoFile {
        val trimmed = raw.trim()
        return if (trimmed.startsWith("{")) {
            json.decodeFromString<MemoFile>(trimmed)
        } else {
            MemoFile(notebook = decodePlain(trimmed))
        }
    }

    private fun decodePlain(raw: String): Notebook {
        val text = raw.replace("\r\n", "\n").replace('\r', '\n')
        val lines = text.lines()
        val firstSection = lines.indexOfFirst { it.trim().startsWith("===") }.let {
            if (it < 0) lines.size else it
        }
        val header = lines.take(firstSection)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val idx = line.indexOf(':')
                if (idx <= 0) null
                else line.substring(0, idx).trim().lowercase() to line.substring(idx + 1).trim()
            }
            .toMap()

        data class Block(val key: String, val title: String, val body: StringBuilder = StringBuilder())
        val blocks = mutableListOf<Block>()
        var current: Block? = null
        for (line in lines.drop(firstSection)) {
            val trimmed = line.trim()
            if (trimmed.startsWith("===") && trimmed.endsWith("===")) {
                val inner = trimmed.removePrefix("===").removeSuffix("===").trim()
                val isMain = inner.equals("main", true) || inner.equals("메인", true)
                current = Block(if (isMain) "main" else "tab", inner.removePrefix("tab:").trim())
                blocks += current
            } else if (current != null) {
                if (current.body.isNotEmpty()) current.body.append('\n')
                current.body.append(line)
            }
        }

        val main = blocks.firstOrNull { it.key == "main" }?.body?.toString()?.trim().orEmpty()
        val tabs = blocks.filter { it.key == "tab" }.map { block ->
            TabMemo(title = block.title.ifBlank { "탭" }, body = block.body.toString().trim())
        }
        val labels = header["labels"].orEmpty()
            .split(',', '，', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return Notebook(
            title = header["title"].orEmpty(),
            emoji = header["emoji"]?.ifBlank { null } ?: randomEmoji(),
            body = main,
            labels = labels,
            tabs = tabs,
        )
    }
}
