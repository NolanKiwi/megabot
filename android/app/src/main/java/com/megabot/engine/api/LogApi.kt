package com.megabot.engine.api

import android.util.Log

/**
 * Logging API exposed to JavaScript scripts.
 */
class LogApi {

    companion object {
        private const val TAG = "BotScript"
    }

    fun d(message: String) { Log.d(TAG, message) }
    fun debug(message: String) { Log.d(TAG, message) }

    fun i(message: String) { Log.i(TAG, message) }
    fun info(message: String) { Log.i(TAG, message) }

    fun w(message: String) { Log.w(TAG, message) }
    fun warn(message: String) { Log.w(TAG, message) }

    fun e(message: String) { Log.e(TAG, message) }
    fun error(message: String) { Log.e(TAG, message) }
}
