package com.megabot.engine.api

import android.content.Context
import android.util.Log
import com.megabot.service.NotificationListener

/**
 * Bot API exposed to JavaScript scripts.
 * Provides message reply capabilities.
 */
class BotApi(private val context: Context) {

    companion object {
        private const val TAG = "BotApi"
    }

    fun createReplier(packageName: String, room: String): Replier {
        return Replier(packageName, room)
    }

    /**
     * The Replier object passed to response() function.
     * Scripts call replier.reply("text") to send messages.
     */
    inner class Replier(
        private val packageName: String,
        private val room: String
    ) {
        fun reply(message: String): Boolean {
            val listener = NotificationListener.instance
            if (listener == null) {
                Log.w(TAG, "NotificationListener not available")
                return false
            }
            return listener.sendReply(packageName, room, message)
        }

        fun reply(room: String, message: String): Boolean {
            val listener = NotificationListener.instance
            if (listener == null) {
                Log.w(TAG, "NotificationListener not available")
                return false
            }
            return listener.sendReply(packageName, room, message)
        }
    }

    /** Check if a reply session exists for the given room */
    fun canReply(packageName: String, room: String): Boolean {
        return NotificationListener.getReplyAction(packageName, room) != null
    }
}
