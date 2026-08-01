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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
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

// Status Data Class
data class StatusItem(
    val id: String,
    val name: String,
    val time: String,
    val isSeen: Boolean,
    val avatarInitials: String
)

@Composable
fun StatusScreen() {
    // Dummy Status Lists
    val recentStatuses = remember {
        listOf(
            StatusItem("1", "Alex Morgan", "Today, 09:45 AM", false, "AM"),
            StatusItem("2", "Sarah Connor", "Today, 08:12 AM", false, "SC"),
            StatusItem("3", "Cyber Squad 🚀", "Yesterday, 11:30 PM", false, "CS")
        )
    }

    val viewedStatuses = remember {
        listOf(
            StatusItem("4", "David Miller", "Yesterday, 04:15 PM", true, "DM"),
            StatusItem("5", "Velora Updates", "Yesterday, 10:00 AM", true, "VU")
        )
    }

    // Background Pulse Animation (Same as HomeScreen)
    val infiniteTransition = rememberInfiniteTransition(label = "EliteStatusPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StatusOrbScale"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StatusOrbAlpha"
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
        // Glowing Neon Background Orb (Matched with HomeScreen)
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
                // My Status Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Add status action */ }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile Avatar with Add Icon Badge
                            Box(modifier = Modifier.size(56.dp)) {
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
                                        text = "ME",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                // Plus Badge
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(0xFF3B82F6), CircleShape)
                                        .border(2.dp, Color(0xFF030712), CircleShape)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Status",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "My status",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap to add status update",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Section Header: Recent updates
                        Text(
                            text = "Recent updates",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // Recent Status Items List
                items(recentStatuses) { status ->
                    StatusRowItem(status = status)
                }

                // Section Header: Viewed updates
                if (viewedStatuses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Viewed updates",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
                        )
                    }

                    items(viewedStatuses) { status ->
                        StatusRowItem(status = status)
                    }
                }
            }

            // Floating Action Buttons for Text and Camera Status
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Small Text Status FAB
                FloatingActionButton(
                    onClick = { /* Text status action */ },
                    modifier = Modifier.size(46.dp),
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Create Text Status", modifier = Modifier.size(20.dp))
                }

                // Main Camera Status FAB
                FloatingActionButton(
                    onClick = { /* Camera status action */ },
                    modifier = Modifier.size(56.dp),
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Capture Status")
                }
            }
        }
    }
}

// Individual Status Row Component with Glassmorphism Card Style
@Composable
fun StatusRowItem(status: StatusItem) {
    val ringColor = if (status.isSeen) Color(0xFF475569) else Color(0xFF22C55E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF1E3A8A))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable { /* Handle status click to view */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Story Ring Border around Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .border(2.5.dp, ringColor, CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = status.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status.time,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}