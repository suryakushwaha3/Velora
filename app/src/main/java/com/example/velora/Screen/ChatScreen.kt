package com.example.velora.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

data class MessageItem(
    val id: String,
    val text: String,
    val sender: String,
    val time: String,
    val mediaUri: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contactName: String,
    contactNumber: String,
    onBackPress: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user_id"

    // 🚀 Unique & Sorted Chat ID generation (Dono users ke liye same chat room banega)
    val otherUserIdentifier = contactNumber.filter { it.isDigit() }
    val chatId = if (currentUserId < otherUserIdentifier) {
        "chat_${currentUserId}_$otherUserIdentifier"
    } else {
        "chat_${otherUserIdentifier}_$currentUserId"
    }

    var messageList by remember { mutableStateOf(listOf<MessageItem>()) }

    // 🚀 Real-time Firebase Listener with DESCENDING order (Latest message top/bottom adjustment ke liye)
    DisposableEffect(chatId) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    messageList = snapshot.documents.map { doc ->
                        MessageItem(
                            id = doc.getString("id") ?: "",
                            text = doc.getString("text") ?: "",
                            sender = doc.getString("sender") ?: "",
                            time = doc.getString("time") ?: "Just now",
                            mediaUri = doc.getString("mediaUri")
                        )
                    }
                }
            }
        onDispose { listener.remove() }
    }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilepath by remember { mutableStateOf("") }

    // 🚀 Media Upload & Send Function (Image & Voice dono ke liye)
    val uploadMediaAndSendMessage = { uri: Uri, textMsg: String, isAudio: Boolean ->
        val storageRef = FirebaseStorage.getInstance().reference
        val path = if (isAudio) "chat_audio/${System.currentTimeMillis()}.3gp" else "chat_images/${System.currentTimeMillis()}.jpg"
        val mediaRef = storageRef.child(path)

        mediaRef.putFile(uri).addOnSuccessListener {
            mediaRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val db = FirebaseFirestore.getInstance()
                val messageData = hashMapOf(
                    "id" to System.currentTimeMillis().toString(),
                    "text" to textMsg,
                    "sender" to currentUserId,
                    "time" to "Just now",
                    "mediaUri" to downloadUrl.toString(),
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("chats").document(chatId).collection("messages").add(messageData)
            }
        }
    }

    // 🎙️ Safe Audio Recorder setup (AAC Encoder support)
    val startRecording = {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            audioFilepath = "${context.externalCacheDir?.absolutePath}/audio_${System.currentTimeMillis()}.3gp"
            try {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(audioFilepath)
                    prepare()
                    start()
                }
                isRecording = true
            } catch (e: IOException) {
                e.printStackTrace()
                isRecording = false
            } catch (e: Exception) {
                e.printStackTrace()
                isRecording = false
            }
        }
    }

    val stopRecording = {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false

                if (audioFilepath.isNotEmpty()) {
                    uploadMediaAndSendMessage(Uri.parse(audioFilepath), "🎵 Voice Message", true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isRecording = false
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startRecording()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadMediaAndSendMessage(it, "Attachment [Image]", false)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle captured photo bitmap if needed
    }

    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF020617), Color(0xFF090D16), Color(0xFF0F172A))
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF334155), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contactName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = contactName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(
                                text = if (isRecording) "Recording audio..." else "last seen today at 7:27 PM",
                                color = if (isRecording) Color(0xFF22C55E) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090D16).copy(alpha = 0.95f))
            )
        },
        bottomBar = {
            Column {
                val suggestions = remember(messageText) { getEmojiSuggestions(messageText) }
                if (messageText.isNotBlank() && suggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF1E293B), CircleShape)
                                    .clickable { messageText += emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D16).copy(alpha = 0.95f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { showEmojiPicker = !showEmojiPicker },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.InsertEmoticon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(22.dp))
                            }

                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { Text(if (isRecording) "Recording..." else "Message", color = Color(0xFF64748B)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 0.dp),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                            }

                            if (messageText.isBlank()) {
                                IconButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Brush.linearGradient(
                                        if (isRecording) listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                        else listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                                    ),
                                    CircleShape
                                )
                                .shadow(4.dp, CircleShape)
                                .then(
                                    if (messageText.isBlank()) {
                                        Modifier.pointerInput(Unit) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val down = event.changes.any { it.pressed }
                                                    if (down && !isRecording) {
                                                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                                            startRecording()
                                                        } else {
                                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                        }
                                                    } else if (!down && isRecording) {
                                                        stopRecording()
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Modifier.clickable {
                                            if (messageText.isNotBlank()) {
                                                val db = FirebaseFirestore.getInstance()
                                                val messageData = hashMapOf(
                                                    "id" to System.currentTimeMillis().toString(),
                                                    "text" to messageText,
                                                    "sender" to currentUserId,
                                                    "time" to "Just now",
                                                    "timestamp" to FieldValue.serverTimestamp()
                                                )
                                                db.collection("chats").document(chatId).collection("messages").add(messageData)
                                                messageText = ""
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (messageText.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (showEmojiPicker) {
                    ComprehensiveEmojiPickerView(onEmojiSelected = { messageText += it })
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                reverseLayout = true
            ) {
                items(
                    items = messageList,
                    key = { it.id }
                ) { message ->
                    ChatBubble(message = message, currentUserId = currentUserId)
                }
            }
        }
    }
}

fun getEmojiSuggestions(text: String): List<String> {
    val lowerText = text.lowercase()
    return when {
        lowerText.contains("heart") || lowerText.contains("love") || lowerText.contains("pyaar") -> listOf("❤️", "💖", "💕", "😍", "🥰", "❤️‍🔥")
        lowerText.contains("smile") || lowerText.contains("laugh") || lowerText.contains("haha") || lowerText.contains("lol") -> listOf("😂", "🤣", "😊", "😃", "😁", "😆")
        lowerText.contains("fire") || lowerText.contains("hot") || lowerText.contains("cool") -> listOf("🔥", "😎", "⚡", "🚀", "✨")
        else -> listOf("👍", "🙏", "🔥", "❤️", "✨", "😊")
    }
}

@Composable
fun ComprehensiveEmojiPickerView(onEmojiSelected: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("Smileys") }

    val emojiCategories = remember {
        mapOf(
            "Smileys" to listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "😉", "😍", "🥰", "😎", "🤩", "🥳", "😏", "😴", "🤔"),
            "Animals" to listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦆"),
            "Food" to listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍒", "🍑", "🍍", "🥥", "🥝", "🍅", "🥑", "🍔", "🍕", "🍿"),
            "Activity" to listOf("⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🏓", "🏸", "🏒", "🏑", "🏏", "🥍", "🏹", "🎣", "🤿", "🥊"),
            "Objects" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "💻", "📱", "📞", "📷", "💡", "🔑", "📦", "📚", "✏️", "📌")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFF0F172A))
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            emojiCategories.keys.forEach { category ->
                Text(
                    text = category,
                    color = if (selectedCategory == category) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { selectedCategory = category }
                        .padding(4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val currentEmojis = emojiCategories[selectedCategory] ?: emptyList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentEmojis, key = { it }) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageItem, currentUserId: String) {
    val isMe = message.sender == currentUserId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .background(
                    color = if (isMe) Color(0xFF1D4ED8).copy(alpha = 0.9f) else Color(0xFF1E293B).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isMe) Color(0xFF60A5FA).copy(alpha = 0.3f) else Color(0xFF334155).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = message.text, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.time,
                color = if (isMe) Color(0xFF93C5FD) else Color(0xFF64748B),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}