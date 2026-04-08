package com.megabot.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val enabled: Boolean = false,
    val targetPackages: String = "", // comma-separated
    val compiledAt: Long? = null,
    val compileError: String? = null,
    val version: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_logs")
data class MessageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: String, // "in" or "out"
    val packageName: String,
    val sender: String,
    val room: String,
    val content: String,
    val isGroupChat: Boolean,
    val scriptId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_sms_logs")
data class CallSmsLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "call" or "sms"
    val target: String,
    val message: String? = null,
    val scriptId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
