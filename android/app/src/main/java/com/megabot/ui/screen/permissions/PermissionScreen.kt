package com.megabot.ui.screen.permissions

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
fun PermissionScreen() {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val permissions = remember(refreshKey) {
        listOf(
            PermissionItem("Notification Access", "", isNotificationAccessEnabled(context), isSpecial = true),
            PermissionItem("Phone Calls", Manifest.permission.CALL_PHONE, hasPermission(context, Manifest.permission.CALL_PHONE)),
            PermissionItem("Send SMS", Manifest.permission.SEND_SMS, hasPermission(context, Manifest.permission.SEND_SMS)),
            PermissionItem("Read SMS", Manifest.permission.READ_SMS, hasPermission(context, Manifest.permission.READ_SMS)),
            PermissionItem("Read Phone State", Manifest.permission.READ_PHONE_STATE, hasPermission(context, Manifest.permission.READ_PHONE_STATE)),
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshKey++ }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Permissions", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "_nolanbot needs these permissions to function properly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        permissions.forEach { perm ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(perm.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (perm.granted) "Granted" else "Not granted",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (perm.granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    if (!perm.granted) {
                        Button(onClick = {
                            if (perm.isSpecial) {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            } else {
                                launcher.launch(perm.permission)
                            }
                        }) {
                            Text("Grant")
                        }
                    }
                }
            }
        }
    }
}

data class PermissionItem(
    val name: String,
    val permission: String,
    val granted: Boolean,
    val isSpecial: Boolean = false
)

private fun hasPermission(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

private fun isNotificationAccessEnabled(context: Context): Boolean {
    val cn = ComponentName(context, "com.megabot.service.NotificationListener")
    val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return enabledListeners?.contains(cn.flattenToString()) == true
}
