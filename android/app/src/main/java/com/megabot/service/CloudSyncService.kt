package com.megabot.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.megabot.MegaBotApplication
import com.megabot.R
import com.megabot.engine.ScriptEngineManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Connects to the MegaBot cloud server via Socket.IO.
 * Receives remote commands (script deploy, SMS, phone call)
 * and relays device events back to the server.
 */
class CloudSyncService : Service() {

    companion object {
        private const val TAG = "CloudSyncService"
        private const val NOTIFICATION_ID = 1002

        var instance: CloudSyncService? = null
    }

    private var socket: Socket? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this

        val notification = NotificationCompat.Builder(this, MegaBotApplication.CHANNEL_BOT_SERVICE)
            .setContentTitle("MegaBot Cloud")
            .setContentText("Connected to cloud server")
            .setSmallIcon(R.drawable.ic_bot)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun connect(serverUrl: String, token: String, deviceId: String) {
        disconnect()

        try {
            val options = IO.Options().apply {
                auth = mapOf(
                    "token" to token,
                    "type" to "device",
                    "deviceId" to deviceId
                )
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 3000
            }

            socket = IO.socket(serverUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.i(TAG, "Connected to cloud server")
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.w(TAG, "Disconnected from cloud server")
            }

            // Receive script deployment
            socket?.on("script:deploy") { args ->
                try {
                    val data = args[0] as JSONObject
                    val scriptId = data.getString("scriptId")
                    val code = data.getString("code")
                    Log.i(TAG, "Script deployed: $scriptId")
                    ScriptEngineManager.instance?.updateScript(scriptId, code)
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling script:deploy", e)
                }
            }

            // Receive script toggle
            socket?.on("script:toggle") { args ->
                try {
                    val data = args[0] as JSONObject
                    val scriptId = data.getString("scriptId")
                    val enabled = data.getBoolean("enabled")
                    serviceScope.launch {
                        ScriptEngineManager.instance?.toggleScript(scriptId, enabled)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling script:toggle", e)
                }
            }

            // Receive phone command from web dashboard
            socket?.on("command:phone") { args ->
                try {
                    val data = args[0] as JSONObject
                    val action = data.getString("action")
                    val number = data.getString("number")
                    if (action == "call") {
                        com.megabot.engine.api.PhoneApi(this).call(number)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling command:phone", e)
                }
            }

            // Receive SMS command from web dashboard
            socket?.on("command:sms") { args ->
                try {
                    val data = args[0] as JSONObject
                    val message = data.getString("message")
                    val numbers = data.getJSONArray("numbers")
                    val smsApi = com.megabot.engine.api.SmsApi(this)
                    for (i in 0 until numbers.length()) {
                        smsApi.send(numbers.getString(i), message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling command:sms", e)
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun emitEvent(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        disconnect()
        instance = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
