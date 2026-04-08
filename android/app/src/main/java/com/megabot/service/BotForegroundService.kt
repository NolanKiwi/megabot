package com.megabot.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.megabot.MegaBotApplication
import com.megabot.R

class BotForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, MegaBotApplication.CHANNEL_BOT_SERVICE)
            .setContentTitle("_nolanbot")
            .setContentText("Bot is running")
            .setSmallIcon(R.drawable.ic_bot)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
