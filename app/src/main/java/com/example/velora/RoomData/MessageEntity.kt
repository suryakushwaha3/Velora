package com.example.velora.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val text: String,
    val time: String,
    val timestamp: Long,
    val contactId: String,
    val contactName: String = "Velora User", // 🔥 Naya field taaki naam save rahe
    val mediaUri: String? = null
)