package com.example.velora.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.velora.socket.SocketHandler

@Composable
fun ChatScreen() {
    var messageText by remember { mutableStateOf("") }
    val messageList = remember { mutableStateListOf<String>() }

    // Main thread handler for safe UI state updates from background socket callbacks
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // Socket instance
    val socket = remember { SocketHandler.getSocket() }

    // Server se message sunne ke liye
    DisposableEffect(Unit) {
        val onNewMessage = io.socket.emitter.Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val msg = args[0].toString()
                // Ensure state modification happens on the Main Thread
                mainHandler.post {
                    messageList.add(msg)
                }
            }
        }

        socket.on("receive_message", onNewMessage)

        onDispose {
            socket.off("receive_message", onNewMessage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Yahan aap LazyColumn lagakar messageList ke messages dikha sakte hain

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )

            Button(onClick = {
                if (messageText.isNotEmpty()) {
                    // Server par message emit karein
                    socket.emit("send_message", messageText)
                    messageText = ""
                }
            }) {
                Text("Send")
            }
        }
    }
}