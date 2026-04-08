package com.megabot.engine.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * SMS API exposed to JavaScript scripts.
 */
class SmsApi(private val context: Context) {

    companion object {
        private const val TAG = "SmsApi"
    }

    /**
     * Send an SMS message. Requires SEND_SMS permission.
     */
    fun send(number: String, message: String): Boolean {
        if (!hasPermission()) {
            Log.w(TAG, "SEND_SMS permission not granted")
            return false
        }

        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            if (parts.size == 1) {
                smsManager.sendTextMessage(sanitizeNumber(number), null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(sanitizeNumber(number), null, parts, null, null)
            }
            Log.i(TAG, "SMS sent to $number")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $number", e)
            false
        }
    }

    /**
     * Send SMS to multiple recipients.
     */
    fun sendMultiple(numbers: Array<String>, message: String): Int {
        var successCount = 0
        for (number in numbers) {
            if (send(number, message)) successCount++
        }
        return successCount
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun sanitizeNumber(number: String): String {
        return number.replace(Regex("[^+\\d]"), "")
    }
}
