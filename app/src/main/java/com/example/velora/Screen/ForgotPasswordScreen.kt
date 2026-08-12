package com.example.velora.Screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.velora.AuthManager.AuthManager

@Composable
fun ForgotPasswordScreen(
    onNavigateBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Multi-Layer Dynamic Background Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "EliteForgotPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ForgotOrbScale"
    )

    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ForgotOrbAlpha"
    )

    // Deep Cyber-Space Premium Gradient Theme
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF030712), // Pitch Dark Obsidian
            Color(0xFF0F172A), // Deep Slate
            Color(0xFF1E3A8A)  // Vivid Royal Blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Glowing Neon Orbs for Depth
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(scaleAnim)
                .blur(120.dp)
                .background(Color(0xFF3B82F6).copy(alpha = alphaAnim), CircleShape)
        )

        // Stunning Top Badge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 55.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(alpha = 0.06f),
                modifier = Modifier
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF38BDF8), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔐 VELORA • PASSWORD RECOVERY",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.8.sp
                    )
                }
            }
        }

        // Smooth Card Entrance Animation
        var visibleState by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            visibleState = true
        }

        AnimatedVisibility(
            visible = visibleState,
            enter = fadeIn(animationSpec = tween(700)) +
                    slideInVertically(animationSpec = tween(700, easing = FastOutSlowInEasing)) { it / 4 } +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(700))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 32.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0xFF3B82F6))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.75f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(35.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "Reset Password",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = "Enter your registered email address and we'll send you a Firebase recovery link.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Email Input Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("name@example.com", color = Color(0xFF475569)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFF3B82F6),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Send Reset Link Button
                    Button(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true

                            // Firebase AuthManager resetPassword call
                            AuthManager.resetPassword(
                                email = email,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Password reset link sent to your email! ✉️", Toast.LENGTH_LONG).show()
                                    onNavigateBackToLogin()
                                },
                                onError = { errorMsg ->
                                    isLoading = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isLoading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF60A5FA))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Send Reset Link",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back to Login Option
                    TextButton(onClick = onNavigateBackToLogin) {
                        Text(
                            text = "Remembered your password? Login",
                            color = Color(0xFF60A5FA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}