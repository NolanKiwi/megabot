package com.megabot

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MegaBotApplication : Application() {

    companion object {
        const val CHANNEL_BOT_SERVICE = "megabot_service"
        const val CHANNEL_BOT_LOGS = "megabot_logs"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_BOT_SERVICE,
            "Bot Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "_nolanbot background service notification"
        }

        val logChannel = NotificationChannel(
            CHANNEL_BOT_LOGS,
            "Bot Logs",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Bot activity notifications"
        }

        manager.createNotificationChannels(listOf(serviceChannel, logChannel))
    }
}
