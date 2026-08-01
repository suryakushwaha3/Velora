package com.example.velora.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.velora.Screen.ChatScreen
import com.example.velora.Screen.HomeScreen
 import com.example.velora.components.StatusScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Agar aapke paas Screen sealed class pehle se hai toh use update kar lein, ya ye routes use karein:
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Status : Screen("status")
    object Calls : Screen("calls")
    object Settings : Screen("settings")
    object Chat : Screen("chat/{contactName}/{contactNumber}") {
        fun createRoute(contactName: String, contactNumber: String): String {
            val encodedName = URLEncoder.encode(contactName, StandardCharsets.UTF_8.toString())
            val encodedNumber = URLEncoder.encode(contactNumber, StandardCharsets.UTF_8.toString())
            return "chat/$encodedName/$encodedNumber"
        }
    }
}

@Composable
fun VeloraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onLogout = onLogout,
                onChatClick = { name, number ->
                    // 🚀 Chat par tap hote hi NavController ke zariye ChatScreen par navigate karega
                    val route = Screen.Chat.createRoute(name, number)
                    navController.navigate(route)
                }
            )
        }

        composable(Screen.Status.route) {
            StatusScreen()
        }

        composable(Screen.Calls.route) {
            // CallsScreen()
        }

        composable(Screen.Settings.route) {
            // SettingsScreen()
        }

        // 🚀 Naya ChatScreen Route Destination with Arguments
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("contactName") { type = NavType.StringType },
                navArgument("contactNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("contactName") ?: ""
            val encodedNumber = backStackEntry.arguments?.getString("contactNumber") ?: ""

            // Decode special characters/spaces in names or numbers safely
            val contactName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val contactNumber = URLDecoder.decode(encodedNumber, StandardCharsets.UTF_8.toString())

            ChatScreen(
                contactName = contactName,
                contactNumber = contactNumber,
                onBackPress = {
                    navController.popBackStack() // Back button press par wapas home screen par jayega
                }
            )
        }
    }
}