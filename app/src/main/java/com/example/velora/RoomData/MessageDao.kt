package com.example.velora.Data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // Yahan return type Flow<List<MessageEntity>> hai (Void/Unit nahi)
    @Query("SELECT * FROM messages WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun getMessagesForContact(contactId: String): Flow<List<MessageEntity>>

    // 🔥 Nayi query: Har unique chat room ka latest message fetch karne ke liye taaki app restart par chats load ho sakein
    @Query("SELECT * FROM messages GROUP BY contactId ORDER BY timestamp DESC")
    fun getAllRecentChats(): Flow<List<MessageEntity>>

    // Insert function suspend fun ho sakta hai aur yeh Unit return kar sakta hai (yeh sahi hai)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}