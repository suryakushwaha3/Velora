package com.example.velora.Screen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.velora.socket.SocketHandler
import com.example.velora.viewModel.ChatManager
import org.json.JSONObject

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val lastMessage: String,
    val time: String,
    val isOnline: Boolean,
    val profileImage: String? = null // 🔥 Profile image URL field added
)

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onChatClick: (name: String, id: String) -> Unit,
    onProfileClick: (name: String, phone: String) -> Unit,
    onNewChatClick: () -> Unit
) {
    val context = LocalContext.current
    val socket = remember { SocketHandler.getSocket() }

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // 🔥 Triple dot dropdown menu state
    var showMenu by remember { mutableStateOf(false) }

    val activeChats = ChatManager.activeChats.collectAsState().value

    // 🔥 App khulte hi Room Database se saare recent chats load karein
    LaunchedEffect(Unit) {
        ChatManager.loadChatsFromDatabase(context)
    }

    DisposableEffect(Unit) {
        val onReceiveMessage = io.socket.emitter.Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    val senderId = data.getString("senderId")
                    val msgText = data.getString("message")
                    val timeStr = data.optString("time", "Just now")

                    val rawSenderName = data.optString("senderName", senderId)
                    val senderName = rawSenderName.replace("+", " ").trim().ifEmpty { senderId }
                    val senderImage = data.optString("profileImage", "")

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        ChatManager.updateActiveChat(
                            id = senderId,
                            name = senderName,
                            lastMessage = msgText,
                            time = timeStr
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val onUserStatusChanged = io.socket.emitter.Emitter.Listener { args ->
            if (args.isNotEmpty()) {
                try {
                    val data = args[0] as JSONObject
                    val userId = data.getString("userId")
                    val isOnline = data.getBoolean("status")

                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        // Online status handling if needed
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        socket.on("receive_message", onReceiveMessage)
        socket.on("user_status", onUserStatusChanged)

        onDispose {
            socket.off("receive_message", onReceiveMessage)
            socket.off("user_status", onUserStatusChanged)
        }
    }

    val filteredChats = if (searchQuery.isBlank()) {
        activeChats
    } else {
        activeChats.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090D16),
            Color(0xFF0F172A),
            Color(0xFF1E293B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF090D16).copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(selectedTab) {
                            0 -> "Velora"
                            1 -> "Status"
                            2 -> "Calls"
                            3 -> "Profile"
                            else -> "Velora"
                        },
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // 🔥 WhatsApp Style Triple Dot Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                                .border(1.dp, Color(0xFF334155), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Starred messages", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                }
                            )
                            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color(0xFFEF4444)) },
                                onClick = {
                                    showMenu = false
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or number...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(targetState = selectedTab, label = "TabContentAnimation") { tab ->
                    when (tab) {
                        0 -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (filteredChats.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (searchQuery.isNotBlank()) "No chats found matching '$searchQuery'" else "No active chats yet.",
                                                color = Color(0xFF64748B),
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tap the '+' button below to start chatting!",
                                                color = Color(0xFF475569),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        items(filteredChats, key = { it.id + it.phoneNumber }) { contact ->
                                            ContactListItem(
                                                contact = contact,
                                                onItemClick = {
                                                    val cleanName = contact.name.replace("+", " ").trim().ifEmpty { "Velora User" }

                                                    val validId = if (contact.id.length > 5 && !contact.id.contains("+")) contact.id else contact.phoneNumber
                                                    val cleanId = validId.replace(Regex("[^a-zA-Z0-9_]"), "")

                                                    ChatManager.updateActiveChat(cleanId, cleanName, contact.lastMessage, "Just now")
                                                    onChatClick(cleanName, cleanId)
                                                },
                                                onAvatarClick = {
                                                    val cleanName = contact.name.replace("+", " ").trim().ifEmpty { "Velora User" }
                                                    onProfileClick(cleanName, contact.phoneNumber)
                                                }
                                            )
                                        }
                                    }
                                }

                                FloatingActionButton(
                                    onClick = onNewChatClick,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(20.dp),
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White,
                                    shape = CircleShape
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
                                }
                            }
                        }
                        1 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Status Screen", color = Color.White, fontSize = 18.sp)
                        }
                        2 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Calls Screen", color = Color.White, fontSize = 18.sp)
                        }
                        3 -> ProfileScreen()
                    }
                }
            }

            NavigationBar(
                containerColor = Color(0xFF090D16),
                tonalElevation = 0.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    label = { Text("Chats", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF60A5FA),
                        unselectedIconColor = Color(0xFF64748B),
                        selectedTextColor = Color(0xFF60A5FA),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF1E293B)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.DonutLarge, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    label = { Text("Status", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF60A5FA),
                        unselectedIconColor = Color(0xFF64748B),
                        selectedTextColor = Color(0xFF60A5FA),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF1E293B)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    label = { Text("Calls", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF60A5FA),
                        unselectedIconColor = Color(0xFF64748B),
                        selectedTextColor = Color(0xFF60A5FA),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF1E293B)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(22.dp)) },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF60A5FA),
                        unselectedIconColor = Color(0xFF64748B),
                        selectedTextColor = Color(0xFF60A5FA),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color(0xFF1E293B)
                    )
                )
            }
        }
    }
}

@Composable
fun ContactListItem(
    contact: ContactItem,
    onItemClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClick() },
        color = Color(0xFF0F172A).copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() }
            ) {
                if (!contact.profileImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = contact.profileImage,
                        contentDescription = "Contact Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                if (contact.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                            .border(2.dp, Color(0xFF0F172A), CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick() }
            ) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.lastMessage,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}