package com.megabot.ui.screen.scripts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.megabot.data.local.db.entity.ScriptEntity

@Composable
fun ScriptListScreen(viewModel: ScriptViewModel = hiltViewModel()) {
    val scripts by viewModel.scripts.collectAsState(initial = emptyList())
    var showEditor by remember { mutableStateOf(false) }
    var editingScript by remember { mutableStateOf<ScriptEntity?>(null) }
    var testResult by remember { mutableStateOf<String?>(null) }

    testResult?.let { result ->
        AlertDialog(
            onDismissRequest = { testResult = null },
            title = { Text("테스트 결과 (msg: \"ping\")") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { testResult = null }) { Text("확인") }
            }
        )
    }

    if (showEditor) {
        ScriptEditorDialog(
            script = editingScript,
            onDismiss = { showEditor = false; editingScript = null },
            onSave = { name, code, targets ->
                if (editingScript != null) {
                    viewModel.updateScript(editingScript!!.id, name, code, targets)
                } else {
                    viewModel.createScript(name, code, targets)
                }
                showEditor = false
                editingScript = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Script")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Scripts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (scripts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No scripts yet. Tap + to create one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scripts) { script ->
                        ScriptCard(
                            script = script,
                            onToggle = { viewModel.toggleScript(script.id, it) },
                            onEdit = { editingScript = script; showEditor = true },
                            onDelete = { viewModel.deleteScript(script.id) },
                            onTest = { viewModel.testScript(script) { testResult = it } }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptCard(
    script: ScriptEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(script.name, style = MaterialTheme.typography.titleMedium)
                if (script.compileError != null) {
                    Text(
                        "Error: ${script.compileError}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (script.compiledAt != null) {
                    Text(
                        "Compiled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onTest) { Text("Test") }
                Switch(checked = script.enabled, onCheckedChange = onToggle)
            }
        }
    }
}

@Composable
fun ScriptEditorDialog(
    script: ScriptEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, targets: String) -> Unit
) {
    var name by remember { mutableStateOf(script?.name ?: "") }
    var code by remember { mutableStateOf(script?.code ?: DEFAULT_SCRIPT) }
    var targets by remember { mutableStateOf(script?.targetPackages ?: "com.kakao.talk") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (script == null) "New Script" else "Edit Script") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = targets, onValueChange = { targets = it }, label = { Text("Target Packages") }, singleLine = true)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    modifier = Modifier.height(200.dp),
                    maxLines = 20
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, code, targets) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private val DEFAULT_SCRIPT = """
function response(room, msg, sender, isGroupChat, replier, imageDB, packageName) {
    if (msg == "ping") {
        replier.reply("pong");
    }
}
""".trimIndent()
