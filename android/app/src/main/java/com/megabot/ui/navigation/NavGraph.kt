package com.megabot.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.megabot.ui.screen.home.HomeScreen
import com.megabot.ui.screen.scripts.ScriptListScreen
import com.megabot.ui.screen.logs.LogScreen
import com.megabot.ui.screen.permissions.PermissionScreen
import com.megabot.ui.screen.settings.SettingsScreen

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Scripts : Screen("scripts", "Scripts")
    object Logs : Screen("logs", "Logs")
    object Permissions : Screen("permissions", "Permissions")
    object Settings : Screen("settings", "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    var selectedRoute by remember { mutableStateOf(Screen.Home.route) }

    val screens = listOf(
        Screen.Home to Icons.Default.Home,
        Screen.Scripts to Icons.Default.Code,
        Screen.Logs to Icons.Default.List,
        Screen.Permissions to Icons.Default.Security,
        Screen.Settings to Icons.Default.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedRoute == screen.route,
                        onClick = {
                            selectedRoute = screen.route
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Scripts.route) { ScriptListScreen() }
            composable(Screen.Logs.route) { LogScreen() }
            composable(Screen.Permissions.route) { PermissionScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
