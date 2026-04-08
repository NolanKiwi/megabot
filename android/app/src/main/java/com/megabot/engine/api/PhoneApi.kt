package com.megabot.engine.api

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Phone call API exposed to JavaScript scripts.
 */
class PhoneApi(private val context: Context) {

    companion object {
        private const val TAG = "PhoneApi"
    }

    /**
     * Make a phone call directly. Requires CALL_PHONE permission.
     */
    fun call(number: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "CALL_PHONE permission not granted")
            return false
        }

        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${sanitizeNumber(number)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "Calling: $number")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call $number", e)
            false
        }
    }

    /**
     * Open the dialer with a number. Does NOT require permission.
     */
    fun dial(number: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${sanitizeNumber(number)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open dialer for $number", e)
            false
        }
    }

    private fun sanitizeNumber(number: String): String {
        return number.replace(Regex("[^+\\d]"), "")
    }
}
