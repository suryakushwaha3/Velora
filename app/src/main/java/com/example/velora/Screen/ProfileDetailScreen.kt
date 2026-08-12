package com.example.velora.Screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.velora.Model.UserProfile

// 🔥 Robust Base64 to Bitmap converter (handles both prefixed and raw strings)
private fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val pureBase64 = if (base64String.contains(",")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ProfileDetailScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    onMessageClick: () -> Unit = {},
    onAudioCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isBlocked by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val name = userProfile.name ?: "User"
    val phoneNumber = userProfile.phone ?: "No phone number"
    val profileImageUrl = userProfile.photoUri
    val usernameId = userProfile.username

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E293B))
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
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isBlocked) "Unblock contact" else "Block contact",
                                    color = Color.White
                                )
                            },
                            onClick = {
                                isBlocked = !isBlocked
                                showMenu = false
                                Toast.makeText(
                                    context,
                                    if (isBlocked) "$name blocked" else "$name unblocked",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Report contact", color = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                Toast.makeText(context, "Contact reported", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image Box with Smart URL/Base64 Handling
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUrl.isNullOrEmpty()) {
                        if (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://")) {
                            // Agar valid URL hai toh AsyncImage use karein
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Agar Base64 string hai toh Bitmap mein convert karke dikhayein
                            val bitmap = remember(profileImageUrl) {
                                base64ToBitmap(profileImageUrl)
                            }
                            if (bitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = name.take(2).uppercase(),
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = name.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                if (!usernameId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "@$usernameId", color = Color(0xFF3B82F6), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = phoneNumber, color = Color(0xFF94A3B8), fontSize = 15.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionIcon(Icons.Default.ChatBubble, "Message", onClick = onMessageClick)
                ActionIcon(Icons.Default.Call, "Audio", onClick = onAudioCallClick)
                ActionIcon(Icons.Default.Person, "Video", onClick = onVideoCallClick)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoCard {
                Column {
                    Text("About and phone number", color = Color(0xFF3B82F6), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Hey there! I am using Velora.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Last seen today at 3:45 PM", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoCard(
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Media feature coming soon", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PermMedia, contentDescription = "Media", tint = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Media, links, and docs", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text("0 Media", color = Color(0xFF64748B), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Encryption", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Messages and calls are end-to-end encrypted.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoCard(
                modifier = Modifier.clickable {
                    isBlocked = !isBlocked
                    Toast.makeText(
                        context,
                        if (isBlocked) "$name blocked" else "$name unblocked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Block",
                        tint = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (isBlocked) "Unblock $name" else "Block $name",
                        color = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoCard(
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Contact reported successfully", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ThumbDown, contentDescription = "Report", tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Report contact",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ActionIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(Color(0xFF1E293B), CircleShape)
                .size(52.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF60A5FA), modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}