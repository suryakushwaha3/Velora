package com.example.velora.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.velora.components.BottomBarItem
import com.example.velora.components.CallsScreen
import com.example.velora.components.ProfileScreen
import com.example.velora.components.StatusScreen
import com.example.velora.components.VeloraBottomBar
import com.example.velora.components.VeloraTopBar
import com.google.firebase.auth.FirebaseAuth

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val lastMessage: String,
    val time: String,
    val isOnline: Boolean
)

@SuppressLint("ContextCastToActivity")
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onChatClick: (name: String, phoneNumber: String) -> Unit
) {
    val context = LocalContext.current as Activity

    DisposableEffect(Unit) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        onDispose {}
    }

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Contact List and Permission States
    var contactList by remember { mutableStateOf<List<ContactItem>>(emptyList()) }
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
        if (isGranted) {
            contactList = fetchDeviceContacts(context)
        }
    }

    // Automatically fetch contacts if permission is already granted, else request it
    LaunchedEffect(hasContactPermission) {
        if (hasContactPermission) {
            contactList = fetchDeviceContacts(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    val filteredContacts = contactList.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val infiniteTransition = rememberInfiniteTransition(label = "EliteHomePulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HomeOrbScale"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HomeOrbAlpha"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF030712),
            Color(0xFF0F172A),
            Color(0xFF1E3A8A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .size(250.dp)
                .scale(scaleAnim)
                .blur(100.dp)
                .background(Color(0xFF3B82F6).copy(alpha = alphaAnim), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsSystemBarsPadding()
        ) {
            VeloraTopBar(
                onSearchClick = {},
                onSettingsClick = { selectedTab = 3 },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                }
            )

            if (selectedTab == 0) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search phone contacts...", color = Color(0xFF475569)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.8f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(targetState = selectedTab, label = "TabContentAnimation") { tab ->
                    when (tab) {
                        0 -> {
                            if (!hasContactPermission) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Contact permission is required to show contacts.",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                                            Text("Grant Permission")
                                        }
                                    }
                                }
                            } else if (filteredContacts.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No contacts found in your phone.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(filteredContacts, key = { it.id }) { contact ->
                                        ContactListItem(
                                            contact = contact,
                                            onItemClick = {
                                                onChatClick(contact.name, contact.phoneNumber)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> StatusScreen()
                        2 -> CallsScreen()
                        3 -> ProfileScreen(onLogout = onLogout)
                    }
                }
            }

            VeloraBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                items = listOf(
                    BottomBarItem("Chats", Icons.Default.ChatBubble, 3),
                    BottomBarItem("Status", Icons.Default.DonutLarge, 0),
                    BottomBarItem("Calls", Icons.Default.Call, 0),
                    BottomBarItem("Profile", Icons.Default.Person, 0)
                )
            )
        }
    }
}

// Function to fetch device contacts securely
@SuppressLint("Range")
private fun fetchDeviceContacts(activity: Activity): List<ContactItem> {
    val contactList = mutableListOf<ContactItem>()
    val contentResolver = activity.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val seenNumbers = mutableSetOf<String>()

        while (it.moveToNext()) {
            val id = if (idIndex != -1) it.getString(idIndex) else "0"
            val name = if (nameIndex != -1) it.getString(nameIndex) else "Unknown"
            val number = if (numberIndex != -1) it.getString(numberIndex) else ""

            if (seenNumbers.add(number)) {
                contactList.add(
                    ContactItem(
                        id = id,
                        name = name,
                        phoneNumber = number,
                        lastMessage = number,
                        time = "Tap to chat",
                        isOnline = true
                    )
                )
            }
        }
    }
    return contactList
}

@Composable
fun ContactListItem(contact: ContactItem, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E3A8A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { onItemClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(52.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (contact.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                            .border(2.dp, Color(0xFF0F172A), CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.lastMessage,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun Modifier.statusBarsSystemBarsPadding(): Modifier {
    return this.statusBarsPadding().navigationBarsPadding()
}