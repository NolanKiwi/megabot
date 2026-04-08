package com.megabot.engine

import android.content.Context
import android.util.Log
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.db.entity.ScriptEntity
import com.megabot.domain.model.ChatMessage
import kotlinx.coroutines.*

/**
 * Manages all bot scripts: compilation, execution, lifecycle.
 */
class ScriptEngineManager(
    private val context: Context,
    private val scriptDao: ScriptDao
) {
    companion object {
        private const val TAG = "ScriptEngineManager"
        var instance: ScriptEngineManager? = null
    }

    private val compiledScripts = mutableMapOf<String, CompiledScript>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    data class CompiledScript(
        val entity: ScriptEntity,
        val engine: ScriptEngine
    )

    init {
        instance = this
    }

    /** Load and compile all enabled scripts from DB */
    suspend fun loadAllScripts() {
        val scripts = scriptDao.getEnabledScripts()
        scripts.forEach { compileScript(it) }
        Log.i(TAG, "Loaded ${compiledScripts.size} scripts")
    }

    /** Compile a single script */
    fun compileScript(entity: ScriptEntity): Boolean {
        return try {
            val engine = ScriptEngine(context)
            engine.compile(entity.code, entity.name)

            compiledScripts[entity.id] = CompiledScript(entity, engine)

            scope.launch {
                scriptDao.setCompileResult(entity.id, null, System.currentTimeMillis())
            }
            Log.i(TAG, "Compiled script: ${entity.name}")
            true
        } catch (e: Exception) {
            val error = e.message ?: "Unknown compile error"
            scope.launch {
                scriptDao.setCompileResult(entity.id, error, null)
            }
            Log.e(TAG, "Compile error for ${entity.name}: $error")
            false
        }
    }

    /** Called by NotificationListener when a message arrives */
    fun onMessage(message: ChatMessage) {
        compiledScripts.values.forEach { compiled ->
            if (!compiled.entity.enabled) return@forEach

            val targets = compiled.entity.targetPackages.split(",").map { it.trim() }
            if (targets.isNotEmpty() && message.packageName !in targets) return@forEach

            scope.launch {
                try {
                    compiled.engine.executeResponse(
                        room = message.room,
                        msg = message.content,
                        sender = message.sender,
                        isGroupChat = message.isGroupChat,
                        packageName = message.packageName,
                        imageBase64 = message.imageBase64
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Script ${compiled.entity.name} error: ${e.message}")
                }
            }
        }
    }

    /** Toggle a script on/off */
    suspend fun toggleScript(id: String, enabled: Boolean) {
        scriptDao.setEnabled(id, enabled)
        if (!enabled) {
            compiledScripts.remove(id)
        } else {
            val entity = scriptDao.getScriptById(id) ?: return
            compileScript(entity)
        }
    }

    /** Update script code and recompile */
    suspend fun updateScript(id: String, code: String) {
        val entity = scriptDao.getScriptById(id) ?: return
        val updated = entity.copy(code = code, updatedAt = System.currentTimeMillis())
        scriptDao.upsertScript(updated)
        if (updated.enabled) {
            compileScript(updated)
        }
    }

    /**
     * Simulate a test message against a specific script.
     * Always compiles a fresh engine so the test works even if Bot Service is off.
     * Calls onReply with the script's reply text, or "" if no reply.
     */
    fun simulateMessage(
        entity: ScriptEntity,
        msg: String = "ping",
        room: String = "테스트방",
        sender: String = "테스터",
        packageName: String = "com.kakao.talk",
        onReply: (String) -> Unit
    ) {
        scope.launch {
            try {
                val engine = ScriptEngine(context)
                engine.compile(entity.code, entity.name)
                var replied = false
                engine.executeResponseTest(room, msg, sender, false, packageName) { reply ->
                    replied = true
                    onReply(reply)
                }
                if (!replied) onReply("")
            } catch (e: Exception) {
                onReply("오류: ${e.message}")
            }
        }
    }

    fun destroy() {
        scope.cancel()
        compiledScripts.clear()
        instance = null
    }
}
