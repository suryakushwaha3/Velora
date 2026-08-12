package com.example.velora.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Person
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
                    AppScreen.Home.route -> 0
                    AppScreen.Status.route -> 1
                    AppScreen.Calls.route -> 2
                    AppScreen.Profile.route -> 3
                    else -> 0
                },
                onTabSelected = { index ->
                    val targetRoute = when (index) {
                        0 -> AppScreen.Home.route
                        1 -> AppScreen.Status.route
                        2 -> AppScreen.Calls.route
                        3 -> AppScreen.Profile.route
                        else -> AppScreen.Home.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                items = listOf(
                    BottomBarItem("Chats", Icons.Default.ChatBubble, 0),
                    BottomBarItem("Status", Icons.Default.DonutLarge, 0),
                    BottomBarItem("Calls", Icons.Default.Call, 0),
                    BottomBarItem("Profile", Icons.Default.Person, 0)
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