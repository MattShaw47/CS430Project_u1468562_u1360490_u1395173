package com.example.drawingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.data.SettingsDataStore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun Settings(navController: NavController) {
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore.getInstance(context) }
    val userSettings by settingsDataStore.userSettingsFlow.collectAsState(
        initial = com.example.drawingapp.data.UserSettings()
    )

    val scope = rememberCoroutineScope()
    var showAccountDialog by remember { mutableStateOf(false) }

    val auth = Firebase.auth
    val currentUser = auth.currentUser

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Cloud Backup Setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("User Account", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Show Firebase email, or not signed in
                    val emailText = currentUser?.email ?: "Not signed in"
                    Text(
                        text = "Email: $emailText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (userSettings.userAccountName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Display name: ${userSettings.userAccountName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentUser == null) {
                        Button(
                            onClick = {
                                navController.navigate("auth")
                            }
                        ) {
                            Text("Sign in / Create account")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showAccountDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Change display name")
                            }

                            OutlinedButton(
                                onClick = {
                                    auth.signOut()
                                    // resets display name on sign out
                                    scope.launch {
                                        settingsDataStore.updateUserAccountName("")
                                    }
                                    navController.navigate("auth") {
                                        popUpTo("mainMenu") { inclusive = true }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign out")
                            }
                        }
                    }
                }
            }

            // User Account Setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("User Account", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userSettings.userAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAccountDialog = true }) {
                        Text("Manage Account")
                    }
                }
            }

            // Syncing Setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Syncing", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Sync your drawings across devices",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = userSettings.syncEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                settingsDataStore.updateSync(enabled)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back button
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Main Menu")
            }
        }
    }

    // Account name dialog
    if (showAccountDialog) {
        var newName by remember { mutableStateOf(userSettings.userAccountName) }

        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Change Account Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Account Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            settingsDataStore.updateUserAccountName(newName)
                        }
                        showAccountDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}