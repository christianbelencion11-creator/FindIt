package com.example.findit.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.findit.R
import com.example.findit.auth.FirebaseAuthRepository
import com.example.findit.util.AuthPreferences
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepository: FirebaseAuthRepository,
    authPreferences: AuthPreferences,
    onRestoreSession: () -> Unit = {},
    onNavigateToGetStarted: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToUnlock: () -> Unit,
    onNavigateToSetupPin: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        val uid = authRepository.currentUser?.uid.orEmpty()
        when {
            !authRepository.isFirebaseSignedIn() || uid.isBlank() -> {
                if (authPreferences.isLoggedIn()) {
                    authPreferences.logout()
                }
                if (authPreferences.hasSeenGetStarted()) {
                    onNavigateToLogin()
                } else {
                    onNavigateToGetStarted()
                }
            }
            authPreferences.isPinSet(uid) -> {
                onRestoreSession()
                onNavigateToUnlock()
            }
            else -> {
                onRestoreSession()
                onNavigateToSetupPin()
            }
        }
    }

    val logoAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900),
        label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.75f,
        animationSpec = tween(900),
        label = "logo_scale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 400),
        label = "text_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.iremember_splash_logo),
            contentDescription = "IRemember logo",
            modifier = Modifier
                .width(260.dp)
                .height(210.dp)
                .scale(logoScale)
                .alpha(logoAlpha),
            contentScale = ContentScale.Fit
        )
        // Logo asset already includes "IRemember" — pull tagline up under the wordmark.
        Text(
            text = "Never lose track again",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF66756A),
            modifier = Modifier
                .offset(y = (-12).dp)
                .alpha(textAlpha)
        )
    }
}
