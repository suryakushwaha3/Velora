package com.example.velora.Screen

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.velora.R
import com.google.firebase.auth.FirebaseAuth // Firebase Auth import
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current as Activity

    // Fix: Status bar icons ko dark/light background ke mutabiq visible rakhne ke liye EdgeToEdge configure karein
    DisposableEffect(Unit) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        onDispose {}
    }

    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000) // 3 seconds professional delay

        // Firebase session/user check
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Agar user already logged in hai toh direct Home par bhejein
            onNavigateToHome()
        } else {
            // Agar user logged in nahi hai toh Login screen par bhejein
            onNavigateToLogin()
        }
    }

    // Ultra-Premium Deep Space Gradient Background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020617), // Pitch Dark Obsidian
            Color(0xFF0F172A), // Deep Slate Navy
            Color(0xFF1E3A8A)  // Rich Royal Blue Accent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // Multi-layered Glowing Background Elements for Deep Aesthetic Depth
        Box(
            modifier = Modifier
                .size(260.dp)
                .blur(90.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.25f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .blur(50.dp)
                .background(Color(0xFF60A5FA).copy(alpha = 0.15f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 1. Ultra-Modern Logo Container with Soft Rounded Corners & Glowing Border
            AnimatedVisibility(
                visible = startAnimation,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0xFF3B82F6))
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = .05f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF93C5FD), Color.Transparent, Color(0xFF3B82F6))
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(75.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 2. High-End App Name Typography
            AnimatedVisibility(
                visible = startAnimation,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(1000, delayMillis = 200)
                ) + fadeIn(animationSpec = tween(1000, delayMillis = 200))
            ) {
                Text(
                    text = "Velora Chat",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Sleek Subtitle Tagline
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 400))
            ) {
                Text(
                    text = "Connect instantly. Securely. Effortlessly.",
                    color = Color(0xFF93C5FD),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // 4. Bottom Modern Loading & Status Indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 52.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF60A5FA),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "End-to-End Encrypted",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}