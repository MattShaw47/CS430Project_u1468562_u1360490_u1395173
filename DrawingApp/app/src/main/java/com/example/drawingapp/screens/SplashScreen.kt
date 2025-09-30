package com.example.drawingapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    var faded by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (faded) 0f else 1f,
        animationSpec = tween(durationMillis = 700),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        // milliseconds on screen
        delay(1800)
        faded = true
        delay(700)
        navController.navigate("mainMenu") {
            popUpTo("splashScreen") { inclusive = true }
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Temp group name", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Matt, Mack, Haiyang", textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text("Drawing App", fontSize = 42.sp, style = MaterialTheme.typography.headlineLarge)
        }
    }
}
