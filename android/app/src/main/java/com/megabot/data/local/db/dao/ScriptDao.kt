package com.megabot.data.local.db.dao

import androidx.room.*
import com.megabot.data.local.db.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM scripts ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE enabled = 1")
    suspend fun getEnabledScripts(): List<ScriptEntity>

    @Query("SELECT * FROM scripts WHERE id = :id")
    suspend fun getScriptById(id: String): ScriptEntity?

    @Upsert
    suspend fun upsertScript(script: ScriptEntity)

    @Delete
    suspend fun deleteScript(script: ScriptEntity)

    @Query("UPDATE scripts SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE scripts SET compileError = :error, compiledAt = :compiledAt WHERE id = :id")
    suspend fun setCompileResult(id: String, error: String?, compiledAt: Long?)
}
