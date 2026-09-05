package com.tabmemo.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.File
import java.util.UUID

class ImageStore(private val context: Context) {
    private val root = File(context.filesDir, "images")

    fun file(notebookId: String, imageId: String): File =
        File(File(root, notebookId), "$imageId.jpg")

    fun saveFromUri(notebookId: String, uri: Uri): String? {
        val imageId = UUID.randomUUID().toString()
        val dest = file(notebookId, imageId)
        dest.parentFile?.mkdirs()
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val original = BitmapFactory.decodeStream(input) ?: return null
                val scaled = scale(original)
                dest.outputStream().use { output ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
                }
                if (scaled !== original) original.recycle()
            } ?: return null
            imageId
        }.getOrNull()
    }

    fun writeBytes(notebookId: String, imageId: String, bytes: ByteArray): Boolean {
        val dest = file(notebookId, imageId)
        dest.parentFile?.mkdirs()
        return runCatching {
            dest.writeBytes(bytes)
            true
        }.getOrDefault(false)
    }

    fun readBytes(notebookId: String, imageId: String): ByteArray? {
        val dest = file(notebookId, imageId)
        if (!dest.exists()) return null
        return runCatching { dest.readBytes() }.getOrNull()
    }

    fun delete(notebookId: String, imageId: String) {
        file(notebookId, imageId).delete()
    }

    fun deleteNotebook(notebookId: String) {
        File(root, notebookId).deleteRecursively()
    }

    fun encode(notebookId: String, imageId: String): String? {
        val bytes = readBytes(notebookId, imageId) ?: return null
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun decodeToFile(notebookId: String, imageId: String, base64: String): Boolean {
        val bytes = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull() ?: return false
        return writeBytes(notebookId, imageId, bytes)
    }

    fun gc(keep: Map<String, Set<String>>) {
        if (!root.exists()) return
        root.listFiles()?.forEach { folder ->
            val keepIds = keep[folder.name].orEmpty()
            if (keepIds.isEmpty()) {
                folder.deleteRecursively()
                return@forEach
            }
            folder.listFiles()?.forEach { file ->
                val id = file.name.removeSuffix(".jpg")
                if (id !in keepIds) file.delete()
            }
        }
    }

    fun exportMap(notebook: Notebook): Map<String, String> {
        return notebook.allImageIds().mapNotNull { id ->
            encode(notebook.id, id)?.let { id to it }
        }.toMap()
    }

    fun importMap(notebookId: String, images: Map<String, String>) {
        images.forEach { (id, base64) ->
            decodeToFile(notebookId, id, base64)
        }
    }

    private fun scale(source: Bitmap): Bitmap {
        val longest = maxOf(source.width, source.height)
        if (longest <= MAX_EDGE) return source
        val ratio = MAX_EDGE.toFloat() / longest
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    companion object {
        private const val MAX_EDGE = 1600
    }
}
