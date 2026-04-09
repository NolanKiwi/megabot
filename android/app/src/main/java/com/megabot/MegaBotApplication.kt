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
        const val CHANNEL_CALLS = "megabot_calls"
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

        // 전화 알림 — IMPORTANCE_HIGH 필수 (full-screen intent 동작 조건)
        val callChannel = NotificationChannel(
            CHANNEL_CALLS,
            "전화 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "스크립트 자동 전화 알림"
            enableVibration(true)
            setShowBadge(true)
        }

        manager.createNotificationChannels(listOf(serviceChannel, logChannel, callChannel))
    }
}
