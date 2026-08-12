package com.example.velora.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.velora.AuthManager.AuthManager
import com.example.velora.RoomData.AppDatabase
import com.example.velora.Data.MessageEntity
import com.example.velora.Data.MessageItem
import com.example.velora.Screen.ContactItem
import com.example.velora.socket.SocketHandler
import com.google.firebase.auth.FirebaseAuth
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// 🔥 Global Shared Manager jo Home Screen aur Chat Screen ko sync rakhega
object ChatManager {
    private val _activeChats = MutableStateFlow<List<ContactItem>>(emptyList())
    val activeChats: StateFlow<List<ContactItem>> = _activeChats.asStateFlow()

    // 🔥 Database se chats load karne ka function (App restart par chats gayab nahi hongi)
    fun loadChatsFromDatabase(context: Context) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).messageDao()
                dao.getAllRecentChats().collect { entities ->
                    val list = entities.map { entity ->
                        val cleanName = entity.contactName.replace("+", " ").trim()
                        ContactItem(
                            id = entity.contactId,
                            name = if (cleanName.isNotEmpty() && cleanName != "Velora User") cleanName else entity.contactId,
                            phoneNumber = entity.contactId,
                            lastMessage = entity.text,
                            time = entity.time,
                            isOnline = false
                        )
                    }
                    _activeChats.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateActiveChat(id: String, name: String, lastMessage: String, time: String) {
        // 🔥 Name ko saaf karne ka logic: "+" ko hata kar properly format karna
        val cleanName = name.replace("+", " ").trim()

        val currentList = _activeChats.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id || it.phoneNumber == id }

        val updatedItem = ContactItem(
            id = id,
            name = if (cleanName.isNotEmpty() && cleanName != "Velora User") cleanName else id,
            phoneNumber = id,
            lastMessage = lastMessage,
            time = time,
            isOnline = true
        )

        if (index != -1) {
            currentList.removeAt(index)
        }
        currentList.add(0, updatedItem)
        _activeChats.value = currentList
    }
}

data class ChatUiState(
    val messages: List<MessageItem> = emptyList(),
    val isContactTyping: Boolean = false,
    val isOnline: Boolean = false,
    val messageText: String = "",
    val isRecording: Boolean = false,
    val showEmojiPicker: Boolean = false
)

class ChatViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val dao = AppDatabase.getDatabase(application).messageDao()
    private val socket: Socket = SocketHandler.getSocket()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val typingStopHandler = Handler(Looper.getMainLooper())

    private val currentUserId = AuthManager.getCurrentUserId().ifEmpty {
        FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"
    }

    // Navigation se pass kiye gaye arguments
    private val contactId: String = savedStateHandle.get<String>("contactNumber")
        ?: savedStateHandle.get<String>("contactId")
        ?: "default_chat_room"

    // 🔥 Contact name ko retrieve karte hi clean kar lena
    private val rawContactName: String = savedStateHandle.get<String>("contactName") ?: "Velora User"
    private val contactName: String = rawContactName.replace("+", " ").trim().ifEmpty { contactId }

    init {
        loadMessagesFromDatabase()
        setupSocketListeners()
    }

    private fun loadMessagesFromDatabase() {
        viewModelScope.launch {
            dao.getMessagesForContact(contactId).collect { entities ->
                val messageList = entities.map { entity ->
                    MessageItem(
                        id = entity.id,
                        text = entity.text,
                        sender = entity.sender,
                        time = entity.time,
                        timestamp = entity.timestamp,
                        mediaUri = entity.mediaUri
                    )
                }
                _uiState.update { it.copy(messages = messageList) }
            }
        }
    }

    private fun setupSocketListeners() {
        socket.off("receive_message")
        socket.off("typing")
        socket.off("stop_typing")
        socket.off("user_status")

        socket.on("receive_message") { args ->
            if (args.isNotEmpty()) {
                val msgText = args[0].toString()
                val currentTime = getCurrentTime()

                // 🔥 Jab bhi message receive ho, Home Screen ke active list mein top par laayein
                ChatManager.updateActiveChat(contactId, contactName, msgText, currentTime)

                viewModelScope.launch {
                    val incomingMessage = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        sender = "other_user",
                        text = msgText,
                        time = currentTime,
                        timestamp = System.currentTimeMillis(),
                        contactId = contactId,
                        contactName = contactName // 🔥 Real name save hoga database mein
                    )
                    dao.insertMessage(incomingMessage)
                }
            }
        }

        socket.on("typing") { _ ->
            mainHandler.post {
                _uiState.update { it.copy(isContactTyping = true) }
                typingStopHandler.removeCallbacksAndMessages(null)
                typingStopHandler.postDelayed({
                    _uiState.update { it.copy(isContactTyping = false) }
                }, 3000)
            }
        }

        socket.on("stop_typing") { _ ->
            mainHandler.post {
                _uiState.update { it.copy(isContactTyping = false) }
            }
        }

        socket.on("user_status") { args ->
            if (args.isNotEmpty()) {
                val onlineStatus = args[0].toString().toBoolean()
                mainHandler.post {
                    _uiState.update { it.copy(isOnline = onlineStatus) }
                }
            }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
        if (newText.isNotBlank()) {
            socket.emit("typing", contactId)
        } else {
            socket.emit("stop_typing", contactId)
        }
    }

    fun sendMessage() {
        val currentText = _uiState.value.messageText
        if (currentText.isNotBlank()) {
            socket.emit("send_message", currentText)
            socket.emit("stop_typing", contactId)

            val currentTime = getCurrentTime()

            // 🔥 Jab bhi message send ho, turant Home Screen ke active list mein update karein
            ChatManager.updateActiveChat(contactId, contactName, currentText, currentTime)

            viewModelScope.launch {
                val myMessage = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    sender = currentUserId,
                    text = currentText,
                    time = currentTime,
                    timestamp = System.currentTimeMillis(),
                    contactId = contactId,
                    contactName = contactName // 🔥 Real name save hoga database mein
                )
                dao.insertMessage(myMessage)
            }

            _uiState.update {
                it.copy(
                    messageText = "",
                    showEmojiPicker = false
                )
            }
        }
    }

    // 🔥 NEW FEATURE: Image / Media Message Send karne ka function (Base64 conversion ke sath)
    fun sendImageMessage(context: Context, imageUri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64String = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                val currentTime = getCurrentTime()
                val messageText = "📷 Photo"

                // Socket par image string ya notification emit karein
                socket.emit("send_message", base64String)
                socket.emit("stop_typing", contactId)

                // Home Screen active chat update
                ChatManager.updateActiveChat(contactId, contactName, messageText, currentTime)

                // Local Room Database mein save karein
                viewModelScope.launch {
                    val mediaMessage = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        sender = currentUserId,
                        text = messageText,
                        time = currentTime,
                        timestamp = System.currentTimeMillis(),
                        contactId = contactId,
                        contactName = contactName,
                        mediaUri = base64String
                    )
                    dao.insertMessage(mediaMessage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleEmojiPicker() {
        _uiState.update { it.copy(showEmojiPicker = !it.showEmojiPicker) }
    }

    fun setRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        socket.off("receive_message")
        socket.off("typing")
        socket.off("stop_typing")
        socket.off("user_status")
    }
}