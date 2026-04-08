package com.megabot.data.local.db.dao

import androidx.room.*
import com.megabot.data.local.db.entity.MessageLogEntity
import com.megabot.data.local.db.entity.CallSmsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageLogDao {
    @Insert
    suspend fun insertMessageLog(log: MessageLogEntity)

    @Query("SELECT * FROM message_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<MessageLogEntity>>

    @Query("SELECT * FROM message_logs WHERE room = :room ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByRoom(room: String, limit: Int = 100): Flow<List<MessageLogEntity>>

    @Query("DELETE FROM message_logs WHERE timestamp < :before")
    suspend fun deleteOldLogs(before: Long)
}

@Dao
interface CallSmsLogDao {
    @Insert
    suspend fun insertLog(log: CallSmsLogEntity)

    @Query("SELECT * FROM call_sms_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<CallSmsLogEntity>>

    @Query("SELECT COUNT(*) FROM call_sms_logs WHERE type = :type AND timestamp > :since")
    suspend fun countSince(type: String, since: Long): Int
}
