package com.example.velora.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.velora.Model.UserProfile // 🔥 Imported UserProfile data class
import com.example.velora.Screen.ChatScreen
import com.example.velora.Screen.ContactListScreen
import com.example.velora.Screen.HomeScreen
import com.example.velora.Screen.ProfileDetailScreen
import com.example.velora.Screen.ProfileScreen
import com.example.velora.components.CallsScreen
import com.example.velora.components.StatusScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun VeloraNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.Home.route,
        modifier = modifier
    ) {
        // 1. Home / Chats Route
        composable(AppScreen.Home.route) {
            HomeScreen(
                onLogout = onLogout,
                onProfileClick = { name, phone ->
                    navController.navigate(AppScreen.ProfileDetail.createRoute(name, phone))
                },
                onChatClick = { name, number ->
                    navController.navigate(AppScreen.Chat.createRoute(name, number))
                },
                onNewChatClick = {
                    navController.navigate(AppScreen.ContactList.route)
                }
            )
        }

        // 2. Status Route
        composable(AppScreen.Status.route) {
            StatusScreen()
        }

        // 3. Calls Route
        composable(AppScreen.Calls.route) {
            CallsScreen()
        }

        // 4. Profile Route
        composable(AppScreen.Profile.route) {
            ProfileScreen(
                onBackPress = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 5. WhatsApp Style Contact Profile Detail Route (Updated to use UserProfile)
        composable(
            route = AppScreen.ProfileDetail.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("name") ?: ""
            val encodedPhone = backStackEntry.arguments?.getString("phone") ?: ""

            val decodedName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val decodedPhone = URLDecoder.decode(encodedPhone, StandardCharsets.UTF_8.toString())

            // 🔥 Creating UserProfile object from nav arguments to match ProfileDetailScreen signature
            val userProfile = UserProfile(
                email = "",
                name = decodedName,
                phone = decodedPhone,
                photoUri = "",
                username = ""
            )

            ProfileDetailScreen(
                userProfile = userProfile, // 👈 Passed UserProfile object here
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Contact List Screen Route
        composable(AppScreen.ContactList.route) {
            ContactListScreen(
                onBack = {
                    navController.popBackStack()
                },
                onContactSelected = { name, number ->
                    com.example.velora.viewModel.ChatManager.updateActiveChat(
                        id = number,
                        name = name,
                        lastMessage = "Tap to chat",
                        time = "Just now"
                    )

                    navController.navigate(AppScreen.Chat.createRoute(name, number)) {
                        popUpTo(AppScreen.Home.route)
                    }
                }
            )
        }

        // 7. Chat Screen Route with Arguments
        composable(
            route = AppScreen.Chat.route,
            arguments = listOf(
                navArgument("contactName") { type = NavType.StringType },
                navArgument("contactNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("contactName") ?: ""
            val encodedNumber = backStackEntry.arguments?.getString("contactNumber") ?: ""

            ChatScreen(
                contactName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString()),
                contactNumber = URLDecoder.decode(encodedNumber, StandardCharsets.UTF_8.toString()),
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}