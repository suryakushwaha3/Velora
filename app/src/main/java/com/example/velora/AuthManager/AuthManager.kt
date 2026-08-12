package com.example.velora.AuthManager

import com.example.velora.Model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    // 1. SignUp User using UserProfile Data Class
    fun signUpUser(
        email: String,
        password: String,
        userName: String,
        usernameId: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanUsernameId = usernameId.trim()

        if (cleanUsernameId.isBlank()) {
            onError("Username ID cannot be empty.")
            return
        }

        database.orderByChild("username").equalTo(cleanUsernameId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onError("This Username ID is already taken. Choose another.")
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { authResult ->
                            val userId = authResult.user?.uid ?: run {
                                onError("Failed to retrieve user ID.")
                                return@addOnSuccessListener
                            }

                            val userProfile = UserProfile(
                                email = email,
                                name = userName,
                                photoUri = "", // Default empty image
                                username = cleanUsernameId,
                                phone = phone
                            )

                            database.child(userId).setValue(userProfile)
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { dbError ->
                                    onError(dbError.localizedMessage ?: "Failed to save user data")
                                }
                        }
                        .addOnFailureListener { authError ->
                            onError(authError.localizedMessage ?: "Registration Failed")
                        }
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Database error occurred")
            }
    }

    // 2. Login User (Supports Email or Username)
    fun loginUser(
        identifier: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (identifier.contains("@")) {
            auth.signInWithEmailAndPassword(identifier, password)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onError(it.localizedMessage ?: "Login Failed") }
        } else {
            database.orderByChild("username").equalTo(identifier.trim())
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val email = child.child("email").getValue(String::class.java)
                            if (email != null) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { onError(it.localizedMessage ?: "Login Failed") }
                                return@addOnSuccessListener
                            }
                        }
                        onError("User profile found, but email is missing.")
                    } else {
                        onError("Username ID not found!")
                    }
                }
                .addOnFailureListener { exception ->
                    onError(exception.localizedMessage ?: "Database error occurred")
                }
        }
    }

    // 3. Search Users by Username for Real-time Chat
    fun searchUsersByUsername(
        query: String,
        onSuccess: (List<UserProfile>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (query.isBlank()) {
            onSuccess(emptyList())
            return
        }

        database.orderByChild("username")
            .startAt(query.trim())
            .endAt(query.trim() + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val userList = mutableListOf<UserProfile>()
                for (child in snapshot.children) {
                    val userProfile = child.getValue(UserProfile::class.java)
                    if (userProfile != null) {
                        userList.add(userProfile)
                    }
                }
                onSuccess(userList)
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to search users")
            }
    }

    // 4. Fetch Current User Profile with Auto-Creation & Manual Fallback
    fun getUserProfile(
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId.isBlank()) {
            onError("User not logged in")
            return
        }

        database.child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    try {
                        val userProfile = snapshot.getValue(UserProfile::class.java)
                        if (userProfile != null) {
                            onSuccess(userProfile)
                        } else {
                            fetchManually(snapshot, onSuccess)
                        }
                    } catch (e: Exception) {
                        fetchManually(snapshot, onSuccess)
                    }
                } else {
                    // Auto-create default profile if missing in database
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val defaultProfile = UserProfile(
                        email = currentUser?.email ?: "",
                        name = currentUser?.displayName ?: "Velora User",
                        photoUri = "",
                        username = "user_${userId.take(6)}",
                        phone = currentUser?.phoneNumber ?: ""
                    )

                    database.child(userId).setValue(defaultProfile)
                        .addOnSuccessListener { onSuccess(defaultProfile) }
                        .addOnFailureListener { onSuccess(defaultProfile) }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to fetch profile")
            }
    }

    // Helper for manual snapshot mapping if automatic deserialization fails
    private fun fetchManually(
        snapshot: com.google.firebase.database.DataSnapshot,
        onSuccess: (UserProfile) -> Unit
    ) {
        val email = snapshot.child("email").getValue(String::class.java) ?: ""
        val name = snapshot.child("name").getValue(String::class.java)
        val photoUri = snapshot.child("photoUri").getValue(String::class.java)
        val username = snapshot.child("username").getValue(String::class.java)
        val phone = snapshot.child("phone").getValue(String::class.java)

        val manualProfile = UserProfile(
            email = email,
            name = name,
            photoUri = photoUri,
            username = username,
            phone = phone
        )
        onSuccess(manualProfile)
    }

    // 5. Save Profile Image as Base64 String directly in Realtime Database (`photoUri`)
    fun updateProfileImage(
        base64Image: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId.isBlank()) {
            onError("User not logged in")
            return
        }

        database.child(userId).child("photoUri").setValue(base64Image)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to update profile image")
            }
    }

    // Password Reset
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Failed to send reset email")
            }
    }
}