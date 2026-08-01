package com.example.velora.Navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class AppScreen(val route: String) {
    object Splash : AppScreen("splash_screen")
    object Login : AppScreen("login_screen")
    object SignUp : AppScreen("signup_screen")
    object ForgotPassword : AppScreen("forgot_password_screen")
    object Home : AppScreen("home_screen")
    object Status : AppScreen("status")
    object Calls : AppScreen("calls")
    object Settings : AppScreen("settings")

    // Dynamic Chat Route with arguments
    object Chat : AppScreen("chat_screen/{contactName}/{contactNumber}") {
        fun createRoute(contactName: String, contactNumber: String): String {
            val encodedName = URLEncoder.encode(contactName, StandardCharsets.UTF_8.toString())
            val encodedNumber = URLEncoder.encode(contactNumber, StandardCharsets.UTF_8.toString())
            return "chat_screen/$encodedName/$encodedNumber"
        }
    }
}