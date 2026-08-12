package com.example.velora.Screen

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.velora.AuthManager.AuthManager
import com.example.velora.Model.UserProfile

@Composable
fun ProfileScreen(
    onBackPress: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    var profileName by remember { mutableStateOf("Loading...") }
    var profileEmail by remember { mutableStateOf("Loading...") }
    var profileUsernameId by remember { mutableStateOf("Loading...") }
    var profilePhone by remember { mutableStateOf("Loading...") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    // 🔥 Gallery se image select hone par Base64 mein convert karke Realtime Database mein save karein
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            isUploading = true
            Toast.makeText(context, "Processing image...", Toast.LENGTH_SHORT).show()

            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val base64String = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

                    AuthManager.updateProfileImage(
                        base64Image = base64String,
                        onSuccess = {
                            profileImageUrl = base64String
                            isUploading = false
                            Toast.makeText(context, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMsg ->
                            isUploading = false
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    isUploading = false
                    Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isUploading = false
                Toast.makeText(context, e.localizedMessage ?: "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 Fetch User Profile using UserProfile data class
    LaunchedEffect(Unit) {
        AuthManager.getUserProfile(
            onSuccess = { userProfile: UserProfile ->
                profileName = userProfile.name ?: "No Name"
                profileEmail = userProfile.email ?: "No Email"
                profileUsernameId = userProfile.username ?: "No Username ID"
                profilePhone = userProfile.phone ?: "No Phone"
                profileImageUrl = userProfile.photoUri
                isLoading = false
            },
            onError = { _ ->
                isLoading = false
            }
        )
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
            .padding(24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF3B82F6),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Image Box
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                            CircleShape
                        )
                        .border(2.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            if (!isUploading) {
                                imagePickerLauncher.launch("image/*")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploading -> {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                        }
                        !profileImageUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Text(
                                text = profileName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp
                            )
                        }
                    }

                    // Camera Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(34.dp)
                            .background(Color(0xFF2563EB), CircleShape)
                            .border(2.dp, Color(0xFF0F172A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profileName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "@$profileUsernameId",
                    color = Color(0xFF3B82F6),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                ProfileInfoCard(label = "Email Address", value = profileEmail)
                Spacer(modifier = Modifier.height(12.dp))
                ProfileInfoCard(label = "Phone Number", value = profilePhone)
                Spacer(modifier = Modifier.height(12.dp))
                ProfileInfoCard(label = "Velora Username ID", value = profileUsernameId)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log Out",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0F172A).copy(alpha = 0.7f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}