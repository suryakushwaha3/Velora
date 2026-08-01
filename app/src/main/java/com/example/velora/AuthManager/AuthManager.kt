package com.example.velora.AuthManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val email: String,
    val name: String? = null,
    val phoneNumber: String? = null
)

object AuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // 1. Fast Login with Firebase Authentication
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || !email.contains("@")) {
            onError("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }

        val cleanEmail = email.trim()

        auth.signInWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError("Invalid email or password")
            }
    }

    // 2. Fetch User Phone Number from Firestore after Login
    fun getUserPhoneNumber(onResult: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val phoneNumber = document.getString("phoneNumber")
                    onResult(phoneNumber)
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } else {
            onResult(null)
        }
    }

    // 3. Fast Sign Up User (Optimized to respond instantly)
    fun signUpUser(
        email: String,
        password: String,
        userName: String? = null,
        phoneNumber: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || !email.contains("@")) {
            onError("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            onError("Password must be at least 6 characters")
            return
        }

        val cleanEmail = email.trim()
        val cleanName = userName?.trim()
        val cleanPhone = phoneNumber?.trim()

        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: cleanEmail

                val userProfile = UserProfile(
                    email = cleanEmail,
                    name = cleanName,
                    phoneNumber = cleanPhone
                )

                // 🚀 Speed Optimization: Turant onSuccess call karein taaki UI fast load ho,
                // aur Firestore mein data background mein silent save ho jaye.
                onSuccess()

                firestore.collection("users").document(userId)
                    .set(userProfile)
                    .addOnFailureListener { e ->
                        // Background failure handling agar zaroorat ho
                    }
            }
            .addOnFailureListener { e ->
                val errorMessage = if (e.message?.contains("email-already-in-use", ignoreCase = true) == true) {
                    "This email is already registered. Please login instead."
                } else {
                    "Sign Up Error: ${e.localizedMessage ?: "Registration failed"}"
                }
                onError(errorMessage)
            }
    }

    // 4. Forgot Password
    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || !email.contains("@")) {
            onError("Please enter a valid email address")
            return
        }

        val cleanEmail = email.trim()

        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError("Invalid email or password")
            }
    }
}