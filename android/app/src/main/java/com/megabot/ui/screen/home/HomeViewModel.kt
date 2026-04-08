package com.megabot.ui.screen.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.megabot.data.local.db.dao.CallSmsLogDao
import com.megabot.data.local.db.dao.MessageLogDao
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.prefs.AppPreferences
import com.megabot.service.BotForegroundService
import com.megabot.service.CloudSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeBots: Int = 0,
    val totalMessages: Int = 0,
    val totalCallsSms: Int = 0,
    val cloudConnected: Boolean = false,
    val serviceRunning: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val scriptDao: ScriptDao,
    private val messageLogDao: MessageLogDao,
    private val callSmsLogDao: CallSmsLogDao,
    private val prefs: AppPreferences
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 저장된 자격증명으로 클라우드 자동 연결
        if (prefs.isConnected) {
            val intent = Intent(application, CloudSyncService::class.java)
            application.startForegroundService(intent)
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                CloudSyncService.instance?.connect(prefs.serverUrl, prefs.deviceToken, prefs.deviceId)
            }
        }

        viewModelScope.launch {
            scriptDao.getAllScripts().collect { scripts ->
                _uiState.update {
                    it.copy(
                        activeBots = scripts.count { s -> s.enabled },
                        cloudConnected = prefs.isConnected
                    )
                }
            }
        }

        viewModelScope.launch {
            messageLogDao.getRecentLogs(1000).collect { logs ->
                _uiState.update { it.copy(totalMessages = logs.size) }
            }
        }

        viewModelScope.launch {
            callSmsLogDao.getRecentLogs(1000).collect { logs ->
                _uiState.update { it.copy(totalCallsSms = logs.size) }
            }
        }
    }

    fun toggleService(running: Boolean) {
        val intent = Intent(application, BotForegroundService::class.java)
        if (running) {
            application.startForegroundService(intent)
            // 봇 서비스와 함께 클라우드도 연결
            if (prefs.isConnected) {
                val cloudIntent = Intent(application, CloudSyncService::class.java)
                application.startForegroundService(cloudIntent)
                viewModelScope.launch {
                    kotlinx.coroutines.delay(500)
                    CloudSyncService.instance?.connect(prefs.serverUrl, prefs.deviceToken, prefs.deviceId)
                }
            }
        } else {
            application.stopService(intent)
        }
        _uiState.update { it.copy(serviceRunning = running) }
    }
}
