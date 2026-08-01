package com.example.velora.Data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val email: String,
    val name: String? = null,
    val mobile: String? = null
)