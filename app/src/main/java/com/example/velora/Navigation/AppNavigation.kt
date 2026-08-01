package com.example.velora.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.velora.Screen.ChatScreen
import com.example.velora.Screen.ForgotPasswordScreen
import com.example.velora.Screen.HomeScreen
import com.example.velora.Screen.LoginScreen
import com.example.velora.Screen.SignUpScreen
import com.example.velora.Screen.SplashScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreen.Splash.route) {

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
                onChatClick = { name, number ->
                    navController.navigate(AppScreen.Chat.createRoute(name, number))
                }
            )
        }

        // 6. Chat Screen Route
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