package com.megabot.ui.screen.scripts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.db.entity.ScriptEntity
import com.megabot.engine.ScriptEngine
import com.megabot.engine.ScriptEngineManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    application: Application,
    private val scriptDao: ScriptDao
) : AndroidViewModel(application) {

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val engine = ScriptEngine(getApplication())
                engine.compile(entity.code, entity.name)
                var replied = false
                engine.executeResponseTest("테스트방", "ping", "테스터", false, "com.kakao.talk") { reply ->
                    replied = true
                    viewModelScope.launch(Dispatchers.Main) { onResult(reply) }
                }
                if (!replied) withContext(Dispatchers.Main) { onResult("(응답 없음)") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult("오류: ${e.message}") }
            }
        }
    }
}
