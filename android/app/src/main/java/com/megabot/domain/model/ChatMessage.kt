package com.megabot.domain.model

data class ChatMessage(
    val sender: String,
    val room: String,
    val content: String,
    val isGroupChat: Boolean,
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null
)

data class ChatRoom(
    val name: String,
    val packageName: String,
    val isGroupChat: Boolean
)

data class BotScript(
    val id: String,
    val name: String,
    val code: String,
    val enabled: Boolean = false,
    val targetPackages: List<String> = emptyList(),
    val compiledAt: Long? = null,
    val compileError: String? = null
)
