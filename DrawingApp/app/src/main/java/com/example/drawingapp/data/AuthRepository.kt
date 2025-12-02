package com.example.drawingapp.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {

    val currentUser get() = auth.currentUser
    val currentUserEmail: String? get() = auth.currentUser?.email
    val isSignedIn: Boolean get() = auth.currentUser != null

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    fun signOut() {
        auth.signOut()
    }
}