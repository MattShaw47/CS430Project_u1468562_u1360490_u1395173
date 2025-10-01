package com.example.drawingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel

@Composable
fun Settings(navController: NavController, viewModel: DrawingAppViewModel) {
    var cloudBackupEnabled by remember { mutableStateOf(false) }
    var syncEnabled by remember { mutableStateOf(false) }
    var userAccountName by remember { mutableStateOf("Guest User") }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cloud Backup", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = cloudBackupEnabled,
                        onCheckedChange = { cloudBackupEnabled = it }
                    )
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
                        text = userAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { /* TODO: Implement account management */ }) {
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
                    Text("Syncing", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = { syncEnabled = it }
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
}