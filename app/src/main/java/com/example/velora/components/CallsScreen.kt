package com.example.velora.components


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
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Call Data Class
data class CallItem(
    val id: String,
    val name: String,
    val time: String,
    val isVideoCall: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val avatarInitials: String
)

@Composable
fun CallsScreen() {
    // Dummy Secure Calls List
    val callList = remember {
        listOf(
            CallItem("1", "Alex Morgan", "Today, 11:20 AM", false, true, false, "AM"),
            CallItem("2", "Cyber Squad 🚀", "Today, 09:15 AM", true, false, false, "CS"),
            CallItem("3", "Sarah Connor", "Yesterday, 08:40 PM", false, false, true, "SC"),
            CallItem("4", "David Miller", "28/06, 04:15 PM", true, true, false, "DM"),
            CallItem("5", "Velora Updates", "25/06, 02:10 PM", false, false, false, "VU")
        )
    }

    // Background Pulse Animation (Consistent with HomeScreen & StatusScreen)
    val infiniteTransition = rememberInfiniteTransition(label = "EliteCallsPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CallsOrbScale"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CallsOrbAlpha"
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
        // Glowing Neon Background Orb
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .size(250.dp)
                .scale(scaleAnim)
                .blur(100.dp)
                .background(Color(0xFF3B82F6).copy(alpha = alphaAnim), CircleShape)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Section Header
                item {
                    Text(
                        text = "Recent secure calls",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Call Logs List
                items(callList) { call ->
                    CallRowItem(call = call)
                }
            }

            // Floating Action Button to Start a New Secure Call
            FloatingActionButton(
                onClick = { /* New call action */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(56.dp),
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = "Start Secure Call")
            }
        }
    }
}

// Individual Call Row Component
@Composable
fun CallRowItem(call: CallItem) {
    // Determine call type icon and color
    val callIcon = when {
        call.isMissed -> Icons.Default.CallReceived
        call.isIncoming -> Icons.Default.CallReceived
        else -> Icons.Default.CallMade
    }

    val iconColor = if (call.isMissed) Color(0xFFEF4444) else Color(0xFF22C55E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E3A8A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { /* Handle call log click */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Initials Box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = call.avatarInitials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = call.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = callIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = call.time,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button (Audio/Video Call back)
            IconButton(
                onClick = { /* Callback action */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = if (call.isVideoCall) Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Back",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}