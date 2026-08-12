package com.example.velora.RoomData

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.velora.Data.MessageEntity
import com.example.velora.Data.MessageDao

@Database(entities = [MessageEntity::class], version = 2, exportSchema = false) // 🔥 Version 2 kiya gaya
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velora_database"
                )
                    .fallbackToDestructiveMigration() // 🔥 Purane data ko safely reset karne ke liye
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}