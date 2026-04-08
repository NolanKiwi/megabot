package com.megabot.engine.api

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * File I/O API exposed to JavaScript scripts.
 * Restricted to the app's storage directory.
 */
class FileApi(private val context: Context) {

    private val baseDir: File
        get() = File(context.getExternalFilesDir(null), "scripts_data").also { it.mkdirs() }

    fun read(path: String): String {
        return resolveFile(path).readText()
    }

    fun write(path: String, data: String) {
        val file = resolveFile(path)
        file.parentFile?.mkdirs()
        file.writeText(data)
    }

    fun append(path: String, data: String) {
        val file = resolveFile(path)
        file.parentFile?.mkdirs()
        file.appendText(data)
    }

    fun remove(path: String): Boolean {
        return resolveFile(path).delete()
    }

    fun exists(path: String): Boolean {
        return resolveFile(path).exists()
    }

    fun createDir(path: String): Boolean {
        return resolveFile(path).mkdirs()
    }

    fun getSdcardPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    private fun resolveFile(path: String): File {
        val file = File(baseDir, path)
        // Prevent path traversal
        require(file.canonicalPath.startsWith(baseDir.canonicalPath)) {
            "Access denied: path traversal attempt"
        }
        return file
    }
}
