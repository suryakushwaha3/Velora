package com.example.velora.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.velora.Model.UserProfile // Import your UserProfile data class
import com.example.velora.Screen.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.Splash.route
    ) {

        // 1. Splash Screen Route
        composable(AppScreen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen Route
        composable(AppScreen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppScreen.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AppScreen.ForgotPassword.route)
                }
            )
        }

        // 3. Forgot Password Screen Route
        composable(AppScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Sign Up Screen Route
        composable(AppScreen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AppScreen.Home.route) {
                        popUpTo(AppScreen.SignUp.route) { inclusive = true }
                        popUpTo(AppScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Home Screen Route
        composable(AppScreen.Home.route) {
            HomeScreen(
                onLogout = {
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(AppScreen.Home.route) { inclusive = true }
                    }
                },
                onProfileClick = { name, phone ->
                    // Agar aapke paas abhi sirf name/phone hai, toh aap ise baki empty values ke sath navigate kara sakte hain
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

        // 6. Profile Screen Route (App bar logout profile)
        composable(AppScreen.Profile.route) {
            ProfileScreen(
                onBackPress = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(AppScreen.Login.route) {
                        popUpTo(AppScreen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // 7. 🔥 WhatsApp Style Contact Profile Detail Route (Updated to pass UserProfile)
        composable(
            route = AppScreen.ProfileDetail.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType },
                navArgument("photoUri") { type = NavType.StringType; nullable = true },
                navArgument("username") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("name") ?: ""
            val encodedPhone = backStackEntry.arguments?.getString("phone") ?: ""
            val encodedPhoto = backStackEntry.arguments?.getString("photoUri") ?: ""
            val encodedUsername = backStackEntry.arguments?.getString("username") ?: ""

            val decodedName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val decodedPhone = URLDecoder.decode(encodedPhone, StandardCharsets.UTF_8.toString())
            val decodedPhoto = URLDecoder.decode(encodedPhoto, StandardCharsets.UTF_8.toString())
            val decodedUsername = URLDecoder.decode(encodedUsername, StandardCharsets.UTF_8.toString())

            // Create UserProfile object from nav arguments
            val userProfile = UserProfile(
                email = "", // Email optional ya argument add kar sakte hain
                name = decodedName,
                phone = decodedPhone,
                photoUri = decodedPhoto,
                username = decodedUsername
            )

            ProfileDetailScreen(
                userProfile = userProfile, // 🔥 Passed UserProfile object directly
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 8. Contact List Screen Route
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

        // 9. Chat Screen Route with Arguments
        composable(
            route = AppScreen.Chat.route,
            arguments = listOf(
                navArgument("contactName") { type = NavType.StringType },
                navArgument("contactNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("contactName") ?: ""
            val encodedNumber = backStackEntry.arguments?.getString("contactNumber") ?: ""

            val contactName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            val contactNumber = URLDecoder.decode(encodedNumber, StandardCharsets.UTF_8.toString())

            ChatScreen(
                contactName = contactName,
                contactNumber = contactNumber,
                onBackPress = {
                    navController.popBackStack()
                }
            )
        }
    }
}