package com.megabot.ui.screen.settings

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.megabot.data.local.prefs.AppPreferences
import com.megabot.service.CloudSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val pairingCode: String = "",
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val prefs: AppPreferences
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        _uiState.update {
            it.copy(
                serverUrl = prefs.serverUrl,
                isConnected = prefs.isConnected
            )
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
    }

    fun onPairingCodeChange(code: String) {
        _uiState.update { it.copy(pairingCode = code) }
    }

    fun connectToCloud() {
        val serverUrl = _uiState.value.serverUrl.trim().trimEnd('/')
        val pairingCode = _uiState.value.pairingCode.trim()

        if (serverUrl.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "서버 URL을 입력해주세요") }
            return
        }
        if (pairingCode.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "페어링 코드를 입력해주세요") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                val androidId = android.provider.Settings.Secure.getString(
                    application.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: Build.ID
                val model = "${Build.MANUFACTURER} ${Build.MODEL}"
                val osVersion = "Android ${Build.VERSION.RELEASE}"
                val appVersion = "1.0.0"

                val body = JSONObject().apply {
                    put("pairingCode", pairingCode)
                    put("androidId", androidId)
                    put("name", model)
                    put("model", model)
                    put("osVersion", osVersion)
                    put("appVersion", appVersion)
                }.toString()

                val request = Request.Builder()
                    .url("$serverUrl/api/devices/pair")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = withContext(Dispatchers.IO) {
                    httpClient.newCall(request).execute()
                }

                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val deviceId = json.getString("deviceId")
                    val token = json.getString("token")

                    // 자격증명 저장
                    prefs.serverUrl = serverUrl
                    prefs.deviceId = deviceId
                    prefs.deviceToken = token

                    // CloudSyncService 시작 및 연결
                    val intent = Intent(application, CloudSyncService::class.java)
                    application.startForegroundService(intent)

                    // 서비스가 시작될 시간을 약간 기다린 뒤 연결
                    withContext(Dispatchers.IO) { Thread.sleep(500) }
                    CloudSyncService.instance?.connect(serverUrl, token, deviceId)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isConnected = true,
                            pairingCode = "",
                            successMessage = "클라우드에 성공적으로 연결됐어요!"
                        )
                    }
                } else {
                    val error = try {
                        JSONObject(responseBody).optString("error", "연결에 실패했어요")
                    } catch (e: Exception) {
                        "연결에 실패했어요 (${response.code})"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "연결 오류: ${e.message}"
                    )
                }
            }
        }
    }

    fun disconnect() {
        CloudSyncService.instance?.disconnect()
        val intent = Intent(application, CloudSyncService::class.java)
        application.stopService(intent)
        prefs.clear()
        _uiState.update {
            it.copy(
                serverUrl = "",
                isConnected = false,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
