package com.megabot.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.megabot.data.local.db.dao.CallSmsLogDao
import com.megabot.data.local.db.dao.MessageLogDao
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.db.entity.CallSmsLogEntity
import com.megabot.data.local.db.entity.MessageLogEntity
import com.megabot.data.local.db.entity.ScriptEntity

@Database(
    entities = [ScriptEntity::class, MessageLogEntity::class, CallSmsLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MegaBotDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
    abstract fun messageLogDao(): MessageLogDao
    abstract fun callSmsLogDao(): CallSmsLogDao
}
