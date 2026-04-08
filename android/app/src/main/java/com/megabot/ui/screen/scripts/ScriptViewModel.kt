package com.megabot.ui.screen.scripts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.db.entity.ScriptEntity
import com.megabot.engine.ScriptEngineManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val scriptDao: ScriptDao
) : ViewModel() {

    val scripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    fun createScript(name: String, code: String, targets: String) {
        viewModelScope.launch {
            val entity = ScriptEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                code = code,
                targetPackages = targets
            )
            scriptDao.upsertScript(entity)
        }
    }

    fun updateScript(id: String, name: String, code: String, targets: String) {
        viewModelScope.launch {
            val existing = scriptDao.getScriptById(id) ?: return@launch
            val updated = existing.copy(
                name = name,
                code = code,
                targetPackages = targets,
                updatedAt = System.currentTimeMillis()
            )
            scriptDao.upsertScript(updated)
            ScriptEngineManager.instance?.updateScript(id, code)
        }
    }

    fun toggleScript(id: String, enabled: Boolean) {
        viewModelScope.launch {
            ScriptEngineManager.instance?.toggleScript(id, enabled)
        }
    }

    fun deleteScript(id: String) {
        viewModelScope.launch {
            val entity = scriptDao.getScriptById(id) ?: return@launch
            scriptDao.deleteScript(entity)
        }
    }

    /** Send "ping" to the script and return reply via onResult (on Main thread) */
    fun testScript(entity: ScriptEntity, onResult: (String) -> Unit) {
        val manager = ScriptEngineManager.instance
        if (manager == null) {
            onResult("Bot Service가 꺼져 있습니다. Home에서 Bot Service를 켜주세요.")
            return
        }
        manager.simulateMessage(entity) { reply ->
            viewModelScope.launch(Dispatchers.Main) {
                onResult(if (reply.isEmpty()) "(응답 없음)" else reply)
            }
        }
    }
}
