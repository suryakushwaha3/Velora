package com.example.velora.Screen

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.velora.AuthManager.AuthManager
import com.example.velora.Data.MessageItem
import com.example.velora.viewModel.ChatUiState
import com.example.velora.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException

@Composable
fun ChatScreen(
    contactName: String,
    contactNumber: String,
    onBackPress: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatContent(
        contactName = contactName,
        contactNumber = contactNumber,
        uiState = uiState,
        onBackPress = onBackPress,
        onMessageChange = viewModel::onMessageTextChanged,
        onSendClick = viewModel::sendMessage,
        onEmojiToggle = viewModel::toggleEmojiPicker,
        onRecordingStateChange = viewModel::setRecording
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    contactName: String,
    contactNumber: String,
    uiState: ChatUiState,
    onBackPress: () -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onEmojiToggle: () -> Unit,
    onRecordingStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val currentUserId = remember {
        AuthManager.getCurrentUserId().ifEmpty {
            FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"
        }
    }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFilepath by remember { mutableStateOf("") }

    val startRecording = {
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
            onRecordingStateChange(true)
        } catch (e: IOException) {
            onRecordingStateChange(false)
        }
    }

    val stopRecording = {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            onRecordingStateChange(false)
        } catch (e: Exception) {
            onRecordingStateChange(false)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) startRecording() }

    val gradientBrush = remember {
        Brush.verticalGradient(colors = listOf(Color(0xFF020617), Color(0xFF090D16), Color(0xFF0F172A)))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            ChatTopBar(
                contactName = contactName,
                contactNumber = contactNumber,
                isContactTyping = uiState.isContactTyping,
                isRecording = uiState.isRecording,
                isOnline = uiState.isOnline,
                onBackPress = onBackPress
            )
        },
        bottomBar = {
            ChatBottomBar(
                messageText = uiState.messageText,
                isRecording = uiState.isRecording,
                showEmojiPicker = uiState.showEmojiPicker,
                onMessageChange = onMessageChange,
                onEmojiToggle = onEmojiToggle,
                onSendClick = onSendClick,
                onMicPress = {
                    val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    if (permission == PackageManager.PERMISSION_GRANTED) startRecording()
                    else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onMicRelease = { stopRecording() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(gradientBrush).padding(innerPadding)) {
            MessageList(messages = uiState.messages, currentUserId = currentUserId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar(
    contactName: String,
    contactNumber: String,
    isContactTyping: Boolean,
    isRecording: Boolean,
    isOnline: Boolean,
    onBackPress: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(38.dp).background(Color(0xFF334155), CircleShape),
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
                Column {
                    Text(text = contactName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = when {
                            isContactTyping -> "typing..."
                            isRecording -> "Recording audio..."
                            isOnline -> "Online"
                            else -> "Offline"
                        },
                        color = if (isContactTyping || isRecording || isOnline) Color(0xFF22C55E) else Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090D16).copy(alpha = 0.95f))
    )
}

@Composable
private fun MessageList(messages: List<MessageItem>, currentUserId: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        reverseLayout = true
    ) {
        items(items = messages, key = { it.id }) { message ->
            ChatBubble(message = message, currentUserId = currentUserId)
        }
    }
}

@Composable
private fun ChatBottomBar(
    messageText: String,
    isRecording: Boolean,
    showEmojiPicker: Boolean,
    onMessageChange: (String) -> Unit,
    onEmojiToggle: () -> Unit,
    onSendClick: () -> Unit,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit
) {
    Column {
        val suggestions = getEmojiSuggestions(messageText)
        if (messageText.isNotBlank() && suggestions.isNotEmpty()) {
            EmojiSuggestions(suggestions) { onMessageChange(messageText + it) }
        }

        Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), color = Color.Transparent) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF090D16).copy(alpha = 0.95f)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MessageInput(
                    messageText = messageText,
                    isRecording = isRecording,
                    onMessageChange = onMessageChange,
                    onEmojiToggle = onEmojiToggle
                )
                Spacer(modifier = Modifier.width(6.dp))
                SendOrMicButton(
                    isMessageEmpty = messageText.isBlank(),
                    isRecording = isRecording,
                    onSendClick = onSendClick,
                    onMicPress = onMicPress,
                    onMicRelease = onMicRelease
                )
            }
        }
        if (showEmojiPicker) ComprehensiveEmojiPickerView { onMessageChange(messageText + it) }
    }
}

@Composable
private fun RowScope.MessageInput(
    messageText: String,
    isRecording: Boolean,
    onMessageChange: (String) -> Unit,
    onEmojiToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEmojiToggle, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.InsertEmoticon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(22.dp))
        }
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageChange,
            placeholder = { Text(if (isRecording) "Recording..." else "Message", color = Color(0xFF64748B)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
private fun SendOrMicButton(
    isMessageEmpty: Boolean,
    isRecording: Boolean,
    onSendClick: () -> Unit,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit
) {
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
                if (isMessageEmpty) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onMicPress()
                                try { awaitRelease() } finally { onMicRelease() }
                            }
                        )
                    }
                } else {
                    Modifier.clickable { onSendClick() }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isMessageEmpty) Icons.Default.Mic else Icons.Default.Send,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmojiSuggestions(suggestions: List<String>, onEmojiClick: (String) -> Unit) {
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
                    .clickable { onEmojiClick(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
        }
    }
}

fun getEmojiSuggestions(text: String): List<String> {
    val lowerText = text.lowercase()
    return when {
        lowerText.contains("heart") || lowerText.contains("love") -> listOf("❤️", "💖", "💕", "😍")
        lowerText.contains("smile") || lowerText.contains("haha") -> listOf("😂", "🤣", "😊", "😃")
        else -> listOf("👍", "🙏", "🔥", "❤️")
    }
}

@Composable
fun ComprehensiveEmojiPickerView(onEmojiSelected: (String) -> Unit) {
    var selectedCategory by remember { mutableStateOf("Smileys") }
    val emojiCategories = remember {
        mapOf(
            "Smileys" to listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇"),
            "Animals" to listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B).copy(alpha = 0.5f)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            emojiCategories.keys.forEach { category ->
                Text(
                    text = category,
                    color = if (selectedCategory == category) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                    modifier = Modifier.clickable { selectedCategory = category }.padding(4.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(emojiCategories[selectedCategory] ?: emptyList(), key = { it }) { emoji ->
                Box(modifier = Modifier.size(40.dp).clickable { onEmojiSelected(emoji) }, contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageItem, currentUserId: String) {
    val isMe = message.sender == currentUserId
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .background(
                    color = if (isMe) Color(0xFF1D4ED8).copy(alpha = 0.9f) else Color(0xFF1E293B).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = message.text, color = Color.White, fontSize = 14.sp)
            Text(
                text = message.time,
                color = if (isMe) Color(0xFF93C5FD) else Color(0xFF64748B),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}