package com.megabot.ui.screen.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.megabot.data.local.db.entity.MessageLogEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogScreen(viewModel: LogViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Message Logs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(logs) { log ->
                LogEntry(log)
            }
        }
    }
}

@Composable
fun LogEntry(log: MessageLogEntity) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dirColor = if (log.direction == "in") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (log.direction == "in") "IN" else "OUT",
                    style = MaterialTheme.typography.labelSmall,
                    color = dirColor
                )
                Text(
                    text = timeFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${log.room} / ${log.sender}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = log.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
