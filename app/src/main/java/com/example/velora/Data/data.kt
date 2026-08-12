package com.example.velora.Model

data class UserProfile(
    val email: String? = "",
    val name: String? = "",
    val photoUri: String? = "",
    val username: String? = "",
    val phone: String? = "",
    val uid: String? = "",        // 🔥 Log warning fix karne ke liye add kiya gaya
    val usernameId: String? = ""  // 🔥 Log warning fix karne ke liye add kiya gaya
)