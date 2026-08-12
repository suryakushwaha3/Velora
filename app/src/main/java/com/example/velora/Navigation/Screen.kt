package com.example.velora.Navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class AppScreen(val route: String) {
    object Splash : AppScreen("splash_screen")
    object Login : AppScreen("login_screen")
    object SignUp : AppScreen("signup_screen")
    object ForgotPassword : AppScreen("forgot_password_screen")
    object Home : AppScreen("home_screen")
    object Profile : AppScreen("profile_screen")
    object Status : AppScreen("status")
    object Calls : AppScreen("calls")
    object Settings : AppScreen("settings")
    object ContactList : AppScreen("contact_list_screen")

    // 🔥 WhatsApp Style Profile Detail Route with Helper Function
    object ProfileDetail : AppScreen("profile_detail/{name}/{phone}") {
        fun createRoute(name: String, phone: String): String {
            val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
            val encodedPhone = URLEncoder.encode(phone, StandardCharsets.UTF_8.toString())
            return "profile_detail/$encodedName/$encodedPhone"
        }
    }

    object Chat : AppScreen("chat_screen/{contactName}/{contactNumber}") {
        fun createRoute(contactName: String, contactNumber: String): String {
            val encodedName = URLEncoder.encode(contactName, StandardCharsets.UTF_8.toString())
            val encodedNumber = URLEncoder.encode(contactNumber, StandardCharsets.UTF_8.toString())
            return "chat_screen/$encodedName/$encodedNumber"
        }
    }
}