package com.example.velora.Data

import java.util.UUID

/**
 * Data model representing a single chat message item.
 * Supports text, sender details, timestamps, and optional media attachments.
 */
data class MessageItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUri: String? = null
)