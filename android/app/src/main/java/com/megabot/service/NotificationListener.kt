package com.megabot.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.megabot.domain.model.ChatMessage
import com.megabot.engine.ScriptEngineManager

/**
 * Core service: intercepts notifications from messenger apps,
 * parses message data, and dispatches to the script engine.
 */
class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        var instance: NotificationListener? = null

        val SUPPORTED_PACKAGES = setOf(
            "com.kakao.talk",
            "jp.naver.line.android",
            "com.facebook.orca",
            "org.telegram.messenger",
            "com.instagram.android"
        )

        // Cached reply actions per room, keyed by "packageName:roomName"
        private val replyActions = mutableMapOf<String, ReplyActionCache>()

        fun getReplyAction(packageName: String, room: String): ReplyActionCache? {
            return replyActions["$packageName:$room"]
        }
    }

    data class ReplyActionCache(
        val action: Notification.Action,
        val remoteInputKey: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in SUPPORTED_PACKAGES) return

        try {
            val notification = sbn.notification
            val extras = notification.extras

            val parsed = parseNotification(sbn.packageName, extras) ?: return

            // Cache reply action
            cacheReplyAction(sbn.packageName, parsed.room, notification)

            Log.d(TAG, "[${parsed.packageName}] ${parsed.room} / ${parsed.sender}: ${parsed.content}")

            // Dispatch to script engine
            ScriptEngineManager.instance?.onMessage(parsed)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Clean up old cached actions (older than 1 hour)
        val cutoff = System.currentTimeMillis() - 3_600_000
        replyActions.entries.removeAll { it.value.timestamp < cutoff }
    }

    private fun parseNotification(packageName: String, extras: Bundle): ChatMessage? {
        return when (packageName) {
            "com.kakao.talk" -> parseKakaoTalk(extras)
            else -> parseGeneric(packageName, extras)
        }
    }

    /**
     * KakaoTalk notification structure:
     * - Group chat: EXTRA_TITLE = sender name, EXTRA_SUB_TEXT = room name
     * - 1:1 chat:   EXTRA_TITLE = sender name, EXTRA_SUB_TEXT = null
     */
    private fun parseKakaoTalk(extras: Bundle): ChatMessage? {
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return null
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return null
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val isGroupChat = subText != null
        val sender = title
        val room = subText ?: title

        return ChatMessage(
            sender = sender,
            room = room,
            content = text,
            isGroupChat = isGroupChat,
            packageName = "com.kakao.talk"
        )
    }

    /**
     * Generic parser for LINE, Facebook Messenger, Telegram, etc.
     */
    private fun parseGeneric(packageName: String, extras: Bundle): ChatMessage? {
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return null
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return null
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        val isGroupChat = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) != null || subText != null

        return ChatMessage(
            sender = title,
            room = subText ?: title,
            content = text,
            isGroupChat = isGroupChat,
            packageName = packageName
        )
    }

    /**
     * Find and cache the Notification.Action with RemoteInput for replying.
     */
    private fun cacheReplyAction(packageName: String, room: String, notification: Notification) {
        val actions = notification.actions ?: return
        for (action in actions) {
            val remoteInputs = RemoteInput.getRemoteInputsFromBundleArray(
                action.extras.getParcelableArray("android.remoteinput.results")
            )
            // Alternative: use action.remoteInputs
            val inputs = action.remoteInputs ?: continue
            if (inputs.isNotEmpty()) {
                replyActions["$packageName:$room"] = ReplyActionCache(
                    action = action,
                    remoteInputKey = inputs[0].resultKey
                )
                return
            }
        }
    }

    /**
     * Send a reply message to a chat room using cached notification action.
     */
    fun sendReply(packageName: String, room: String, message: String): Boolean {
        val cached = replyActions["$packageName:$room"] ?: run {
            Log.w(TAG, "No reply action cached for $packageName:$room")
            return false
        }

        return try {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(cached.remoteInputKey, message)
            RemoteInput.addResultsToIntent(cached.action.remoteInputs, intent, bundle)
            cached.action.actionIntent.send(this, 0, intent)
            Log.d(TAG, "Reply sent to $room: $message")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send reply to $room", e)
            false
        }
    }
}
