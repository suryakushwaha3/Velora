package com.example.velora.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.velora.components.BottomBarItem
import com.example.velora.components.VeloraBottomBar

@Composable
fun MainAppRoot(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            VeloraBottomBar(
                selectedTab = when (currentRoute) {
                    Screen.Home.route -> 0
                    Screen.Status.route -> 1
                    Screen.Calls.route -> 2
                    Screen.Settings.route -> 3
                    else -> 0
                },
                onTabSelected = { index ->
                    val targetRoute = when (index) {
                        0 -> Screen.Home.route
                        1 -> Screen.Status.route
                        2 -> Screen.Calls.route
                        3 -> Screen.Settings.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(targetRoute) {
                        // Backstack ko safely manage karne ke liye latest findStartDestination use kiya gaya hai
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                items = listOf(
                    BottomBarItem("Chats", Icons.Default.ChatBubble, 3),
                    BottomBarItem("Status", Icons.Default.DonutLarge, 0),
                    BottomBarItem("Calls", Icons.Default.Call, 0),
                    BottomBarItem("Settings", Icons.Default.Settings, 0)
                )
            )
        }
    ) { innerPadding ->
        VeloraNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onLogout = onLogout
        )
    }
}