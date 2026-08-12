package com.example.velora.Data

import com.example.velora.socket.SocketHandler
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

/**
 * Repository handling Chat operations via Socket.io.
 * Encapsulates the socket logic away from the ViewModel.
 */
class ChatRepository {
    private val socket: Socket = SocketHandler.getSocket()

    /**
     * Observes incoming messages as a Flow.
     */
    fun observeMessages(): Flow<MessageItem> = callbackFlow {
        val listener = Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val msgText = args[0].toString()
                val message = MessageItem(
                    id = System.currentTimeMillis().toString(),
                    text = msgText,
                    sender = "remote_user", // In a real app, this would come from the payload
                    time = "Just now",
                    timestamp = System.currentTimeMillis()
                )
                trySend(message)
            }
        }
        socket.on("receive_message", listener)
        awaitClose { socket.off("receive_message", listener) }
    }.flowOn(Dispatchers.IO)

    /**
     * Observes typing status of the remote contact.
     */
    fun observeTypingStatus(): Flow<Boolean> = callbackFlow {
        val listener = Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                val isTyping = args[0] as? Boolean ?: false
                trySend(isTyping)
            }
        }
        socket.on("typing_status", listener)
        awaitClose { socket.off("typing_status", listener) }
    }.flowOn(Dispatchers.IO)

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            socket.emit("send_message", text)
        }
    }

    fun setTypingStatus(isTyping: Boolean) {
        socket.emit("typing", isTyping)
    }
}
