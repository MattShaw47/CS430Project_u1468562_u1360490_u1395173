package com.example.drawingapp.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUserEmail: String? get() = auth.currentUser?.email

    /**
     * Signs up a new user with Firebase Authentication and stores the { uid : email } relationship
     * in Firestore in order to query the DB later when sharing images.
     */
    suspend fun signUp(email: String, password: String) {
        val response = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = response.user?.uid ?: return
        val newUserData = mapOf(
            "email" to email.trim()
        )

        firestore.collection("users").document(uid).set(newUserData).await()
    }

    /**
     * Signs existing Firebase user into the system by checking for authentication.
     */
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    /**
     * Signs the user out of the system.
     */
    fun signOut() {
        auth.signOut()
    }
}