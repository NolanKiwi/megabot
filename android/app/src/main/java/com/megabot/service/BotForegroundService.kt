package com.megabot.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.megabot.MegaBotApplication
import com.megabot.R
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.engine.ScriptEngineManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BotForegroundService : Service() {

    companion object {
        private const val TAG = "BotForegroundService"
        const val NOTIFICATION_ID = 1001
    }

    @Inject lateinit var scriptDao: ScriptDao

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this, MegaBotApplication.CHANNEL_BOT_SERVICE)
            .setContentTitle("_nolanbot")
            .setContentText("Bot is running")
            .setSmallIcon(R.drawable.ic_bot)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // ScriptEngineManager 초기화 및 스크립트 로드
        ScriptEngineManager(applicationContext, scriptDao).also {
            scope.launch {
                try {
                    it.loadAllScripts()
                    Log.i(TAG, "Scripts loaded")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load scripts", e)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        ScriptEngineManager.instance?.destroy()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
