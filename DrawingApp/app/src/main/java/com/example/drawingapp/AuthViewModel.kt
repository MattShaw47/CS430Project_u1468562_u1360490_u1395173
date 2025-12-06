package com.example.drawingapp.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drawingapp.data.AuthRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUserEmail: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(
        AuthUiState(currentUserEmail = authRepository.currentUserEmail)
    )
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    /**
     * Authenticates the current user in Firebase.
     */
    fun signIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.signIn(uiState.email, uiState.password)
                uiState = uiState.copy(
                    isLoading = false,
                    currentUserEmail = authRepository.currentUserEmail
                )
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign in failed"
                )
            }
        }
    }

    /**
     * Creates new user in Firebase with { email : password }
     */
    fun signUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                authRepository.signUp(uiState.email, uiState.password)
                uiState = uiState.copy(
                    isLoading = false,
                    currentUserEmail = authRepository.currentUserEmail
                )
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign up failed"
                )
            }
        }
    }
}
