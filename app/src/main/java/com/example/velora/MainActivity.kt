package com.example.velora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.velora.Navigation.AppNavigation
import com.example.velora.socket.SocketHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge display
        enableEdgeToEdge()

        // Socket initialize aur connect karein
        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        setContent {
            // 2. Content ko system bars ke piche tak draw karne ke liye
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 3. Status bar aur navigation bar ke icons ko WHITE karne ke liye
            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = false // false matlab WHITE icons (battery, wifi, time)
            windowInsetsController.isAppearanceLightNavigationBars = false // false matlab navigation icons bhi white

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.closeConnection()
    }
}