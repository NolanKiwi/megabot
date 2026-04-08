package com.megabot.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // 메시지 자동 소거 (3초 후)
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 연결 상태 배지
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cloud Connection", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (uiState.isConnected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
            ) {
                Text(
                    text = if (uiState.isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!uiState.isConnected) {
            // 미연결 상태: 입력 폼
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = viewModel::onServerUrlChange,
                label = { Text("Server URL") },
                placeholder = { Text("http://192.168.0.10:3000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.pairingCode,
                onValueChange = viewModel::onPairingCodeChange,
                label = { Text("Pairing Code") },
                placeholder = { Text("웹 대시보드에서 발급된 6자리 코드") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::connectToCloud,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("연결 중...")
                } else {
                    Text("Connect to Cloud")
                }
            }
        } else {
            // 연결된 상태: 서버 URL 표시 + 연결 해제 버튼
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("연결된 서버", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(uiState.serverUrl, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = viewModel::disconnect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Disconnect")
            }
        }

        // 성공/오류 메시지
        uiState.successMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.15f)
                )
            ) {
                Text(
                    it,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("App Info", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("MegaBot v1.0.0", style = MaterialTheme.typography.bodyMedium)
        Text(
            "Messenger Bot Platform with Phone/SMS & Cloud Dashboard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
