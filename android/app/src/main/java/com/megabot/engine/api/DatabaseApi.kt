package com.megabot.engine.api

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * Simple key-value database API for scripts.
 * Stores data as JSON files.
 */
class DatabaseApi(private val context: Context) {

    private val gson = Gson()
    private val dbDir: File
        get() = File(context.getExternalFilesDir(null), "database").also { it.mkdirs() }

    fun exists(fileName: String): Boolean {
        return resolveFile(fileName).exists()
    }

    fun readString(fileName: String): String? {
        val file = resolveFile(fileName)
        return if (file.exists()) file.readText() else null
    }

    fun readObject(fileName: String): Any? {
        val text = readString(fileName) ?: return null
        return gson.fromJson(text, Any::class.java)
    }

    fun writeString(fileName: String, value: String) {
        resolveFile(fileName).writeText(value)
    }

    fun writeObject(fileName: String, obj: Any) {
        resolveFile(fileName).writeText(gson.toJson(obj))
    }

    fun remove(fileName: String): Boolean {
        return resolveFile(fileName).delete()
    }

    private fun resolveFile(fileName: String): File {
        val sanitized = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(dbDir, sanitized)
    }
}
