package com.megabot.engine.api

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.megabot.MegaBotApplication
import com.megabot.R

/**
 * Phone call API exposed to JavaScript scripts.
 */
class PhoneApi(private val context: Context) {

    companion object {
        private const val TAG = "PhoneApi"
        private const val CALL_NOTIFICATION_ID = 9001
    }

    /**
     * Make a phone call.
     * Android 10+에서 백그라운드 startActivity 차단 → full-screen notification으로 우회
     */
    fun call(number: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "CALL_PHONE permission not granted")
            return false
        }

        val sanitized = sanitizeNumber(number)
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$sanitized")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 직접 startActivity 시도 (포그라운드일 때 작동)
        return try {
            context.startActivity(callIntent)
            Log.i(TAG, "Calling (direct): $number")
            true
        } catch (e: Exception) {
            // 백그라운드 차단 시 full-screen notification으로 우회
            Log.w(TAG, "Direct call blocked, using full-screen notification: ${e.message}")
            showCallNotification(number, sanitized, callIntent)
            true
        }
    }

    /**
     * Full-screen intent 알림으로 전화 걸기 (백그라운드 우회)
     */
    private fun showCallNotification(number: String, sanitized: String, callIntent: Intent) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            CALL_NOTIFICATION_ID,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MegaBotApplication.CHANNEL_CALLS)
            .setContentTitle("📞 전화 걸기")
            .setContentText(number)
            .setSmallIcon(R.drawable.ic_bot)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(CALL_NOTIFICATION_ID, notification)
        Log.i(TAG, "Call notification shown for: $number")
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
