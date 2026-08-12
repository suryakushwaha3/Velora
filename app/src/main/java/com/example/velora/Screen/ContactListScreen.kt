package com.example.velora.Screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage

data class ContactModel(
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null // 🔥 Contact photo support added
)

@Composable
fun ContactListScreen(
    onBack: () -> Unit,
    onContactSelected: (name: String, number: String) -> Unit
) {
    val context = LocalContext.current
    var contactsList by remember { mutableStateOf<List<ContactModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 🔥 Runtime Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            contactsList = fetchPhoneContacts(context)
        }
    }

    // First time check or fetch contacts
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            contactsList = fetchPhoneContacts(context)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // 🔥 Filter contacts based on search query
    val filteredContacts = if (searchQuery.isBlank()) {
        contactsList
    } else {
        contactsList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF090D16), Color(0xFF0F172A))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Select Contact",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search name or number...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!hasPermission) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Contacts permission is required", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else if (contactsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No contacts found on device", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            // Real Contacts List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredContacts, key = { it.phoneNumber }) { contact ->
                    ContactItemView(contact) {
                        onContactSelected(contact.name, contact.phoneNumber)
                    }
                }
            }
        }
    }
}

// 🔥 Helper function to read phone contacts including profile picture URI
fun fetchPhoneContacts(context: Context): List<ContactModel> {
    val contactList = mutableListOf<ContactModel>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

        val seenNumbers = mutableSetOf<String>()

        while (it.moveToNext()) {
            val name = if (nameIndex != -1) it.getString(nameIndex) else "Unknown"
            val number = if (numberIndex != -1) it.getString(numberIndex) else ""
            val photoUri = if (photoIndex != -1) it.getString(photoIndex) else null

            val cleanedNumber = number.replace("\\s".toRegex(), "")
            if (cleanedNumber.isNotEmpty() && seenNumbers.add(cleanedNumber)) {
                contactList.add(ContactModel(name = name, phoneNumber = number, photoUri = photoUri))
            }
        }
    }
    return contactList
}

@Composable
fun ContactItemView(contact: ContactModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            if (!contact.photoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = "Contact Photo",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = contact.phoneNumber, color = Color(0xFF94A3B8), fontSize = 13.sp)
        }
    }
}