package com.example.drawingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.drawingapp.utils.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.collectAsState
import com.example.drawingapp.DrawingAppViewModel

@Composable
fun AuthScreen(
    navController: NavController,
    drawingAppViewModel: DrawingAppViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val state = authViewModel.uiState

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign in", style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = authViewModel::onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = authViewModel::onPasswordChange,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.signIn {
                        navController.navigate("mainMenu") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }

                    // update cloud images for new user
                    drawingAppViewModel.resetCloudDrawingsAndSelection()
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign in")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    authViewModel.signUp {
                        navController.navigate("mainMenu") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }

                    drawingAppViewModel.resetCloudDrawingsAndSelection()
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create account")
            }
        }
    }
}
