package com.example.findit.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.findit.ui.components.AuthShell
import com.example.findit.ui.components.AuthTextColor
import com.example.findit.ui.components.PinDots
import com.example.findit.ui.components.PinKeypad
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.AuthPreferences
import com.example.findit.util.BiometricHelper

private const val PIN_LENGTH = 6

@Composable
fun SetupPinScreen(
    authPreferences: AuthPreferences,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var firstPin by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val activity = LocalContext.current as? FragmentActivity
    val biometricAvailable = remember(activity) {
        activity?.let { BiometricHelper.canAuthenticate(it) } == true
    }
    var enableBiometric by remember(biometricAvailable) { mutableStateOf(biometricAvailable) }

    AuthShell(
        title = if (step == 0) "Create your PIN" else "Confirm your PIN",
        subtitle = "Secure quick access to your items",
        centerContent = true,
        contentTopPadding = 276.dp
    ) {
        PinDots(enteredLength = pin.length, pinLength = PIN_LENGTH)
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        PinKeypad(
            onDigit = { digit ->
                error = null
                if (pin.length < PIN_LENGTH) {
                    pin += digit
                    if (pin.length == PIN_LENGTH) {
                        if (step == 0) {
                            firstPin = pin
                            pin = ""
                            step = 1
                        } else if (pin == firstPin) {
                            authPreferences.setPin(pin)
                            authPreferences.setBiometricEnabled(enableBiometric && biometricAvailable)
                            onComplete()
                        } else {
                            error = "PINs do not match. Try again."
                            pin = ""
                            firstPin = ""
                            step = 0
                        }
                    }
                }
            },
            onBackspace = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            }
        )
        if (biometricAvailable) {
            Spacer(Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = Spacing.sm)
                    )
                    Text(
                        text = "Use fingerprint unlock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuthTextColor
                    )
                }
                Switch(
                    checked = enableBiometric,
                    onCheckedChange = { enableBiometric = it }
                )
            }
        }
    }
}

@Composable
fun UnlockScreen(
    authPreferences: AuthPreferences,
    onUnlocked: () -> Unit,
    onUsePassword: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val activity = LocalContext.current as? FragmentActivity
    val biometricEnabled = authPreferences.isBiometricEnabled()
    val biometricAvailable = remember(activity) {
        activity?.let { BiometricHelper.canAuthenticate(it) } == true
    }

    LaunchedEffect(biometricEnabled, activity) {
        if (biometricEnabled && activity != null) {
            BiometricHelper.showPrompt(
                activity = activity,
                onSuccess = onUnlocked,
                onError = { /* user can use PIN instead */ }
            )
        }
    }

    AuthShell(
        title = "Welcome back",
        subtitle = authPreferences.getEmail(),
        centerContent = true,
        contentTopPadding = 276.dp
    ) {
        PinDots(enteredLength = pin.length, pinLength = PIN_LENGTH)
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }
        Spacer(Modifier.height(Spacing.lg))
        PinKeypad(
            onDigit = { digit ->
                error = null
                if (pin.length < PIN_LENGTH) {
                    pin += digit
                    if (pin.length == PIN_LENGTH) {
                        if (authPreferences.verifyPin(pin)) {
                            onUnlocked()
                        } else {
                            error = "Incorrect PIN. Try again."
                            pin = ""
                        }
                    }
                }
            },
            onBackspace = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
            }
        )
        if (biometricAvailable && activity != null) {
            Spacer(Modifier.height(Spacing.lg))
            OutlinedButton(
                onClick = {
                    BiometricHelper.showPrompt(
                        activity = activity,
                        onSuccess = onUnlocked,
                        onError = { msg -> error = msg }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
                Text("Use fingerprint")
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        TextButton(
            onClick = {
                authPreferences.logout()
                onUsePassword()
            },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Sign in with password")
        }
    }
}
