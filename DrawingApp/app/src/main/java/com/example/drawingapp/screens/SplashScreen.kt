package com.example.drawingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SplashScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Drawing App")

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("mainMenu") },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Enter App")
            }
        }
    }
}