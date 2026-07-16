package com.example.findit.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.findit.auth.SecretRecoveryRepository
import com.example.findit.auth.UsernameAuth
import com.example.findit.ui.components.AuthErrorText
import com.example.findit.ui.components.AuthMutedColor
import com.example.findit.ui.components.AuthShell
import com.example.findit.ui.components.AuthTextColor
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.PasswordUtils
import kotlinx.coroutines.launch

private enum class ForgotPasswordStep {
    AdditionalSecurity,
    RecreatePassword,
    Success
}

@Composable
fun ForgotPasswordScreen(
    recoveryRepository: SecretRecoveryRepository,
    initialUsername: String = "",
    onBackToLogin: () -> Unit
) {
    var step by remember { mutableStateOf(ForgotPasswordStep.AdditionalSecurity) }
    var username by remember { mutableStateOf(initialUsername) }
    var verifiedSecret by remember { mutableStateOf("") }
    var secretDetails by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newSecretDetails by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val title = when (step) {
        ForgotPasswordStep.AdditionalSecurity -> "Additional Security"
        ForgotPasswordStep.RecreatePassword -> "Re-Create Password"
        ForgotPasswordStep.Success -> "Password updated"
    }
    val subtitle = when (step) {
        ForgotPasswordStep.AdditionalSecurity -> "Provide your Secret Details"
        ForgotPasswordStep.RecreatePassword -> "Please Enter your New Password"
        ForgotPasswordStep.Success -> "You can sign in with your new password"
    }

    AuthShell(
        title = title,
        subtitle = subtitle,
        scrollable = true,
        footerLink = if (step == ForgotPasswordStep.Success) {
            null
        } else {
            "Back to Sign in" to onBackToLogin
        }
    ) {
        when (step) {
            ForgotPasswordStep.AdditionalSecurity -> {
                if (initialUsername.isBlank()) {
                    AuthTextField(
                        value = username,
                        onValueChange = { username = it; error = null },
                        label = "Username"
                    )
                }
                AuthTextField(
                    value = secretDetails,
                    onValueChange = { secretDetails = it; error = null },
                    label = "Secret Details"
                )
                error?.let { AuthErrorText(it, modifier = Modifier.padding(top = Spacing.xs)) }
                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = {
                        when {
                            UsernameAuth.normalizeUsername(username).isBlank() ->
                                error = "Please enter your username."
                            secretDetails.isBlank() ->
                                error = "Please enter your secret details."
                            else -> {
                                isLoading = true
                                scope.launch {
                                    when (
                                        val result = recoveryRepository.verifySecret(
                                            username,
                                            secretDetails
                                        )
                                    ) {
                                        is SecretRecoveryRepository.Result.Success -> {
                                            verifiedSecret = secretDetails
                                            error = null
                                            step = ForgotPasswordStep.RecreatePassword
                                        }
                                        is SecretRecoveryRepository.Result.Error ->
                                            error = result.message
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Submit",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cancel")
                }
            }

            ForgotPasswordStep.RecreatePassword -> {
                Text(
                    text = "Account details: ",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AuthTextColor,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = UsernameAuth.normalizeUsername(username),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AuthMutedColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.md)
                )
                AuthSectionLabel("Security")
                AuthTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = "New Password",
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = null
                            )
                        }
                    }
                )
                Text(
                    text = "At least 8 characters with letters and numbers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuthMutedColor,
                    modifier = Modifier.padding(start = Spacing.xs)
                )
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = "Confirm password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(Modifier.height(Spacing.xs))
                AuthSectionLabel("Additional Security (in case password is forgotten)")
                AuthTextField(
                    value = newSecretDetails,
                    onValueChange = { newSecretDetails = it; error = null },
                    label = "Add Secret Details"
                )
                error?.let { AuthErrorText(it, modifier = Modifier.padding(top = Spacing.xs)) }
                Spacer(Modifier.height(Spacing.sm))
                Button(
                    onClick = {
                        when {
                            !PasswordUtils.isStrongEnough(newPassword) ->
                                error = "Password must be at least 8 characters with letters and numbers."
                            newPassword != confirmPassword ->
                                error = "Passwords do not match."
                            else -> {
                                isLoading = true
                                scope.launch {
                                    when (
                                        val result = recoveryRepository.resetPasswordWithSecret(
                                            username = username,
                                            secret = verifiedSecret,
                                            newPassword = newPassword,
                                            newSecret = newSecretDetails.ifBlank { null }
                                        )
                                    ) {
                                        is SecretRecoveryRepository.Result.Success -> {
                                            error = null
                                            step = ForgotPasswordStep.Success
                                        }
                                        is SecretRecoveryRepository.Result.Error ->
                                            error = result.message
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Change Password",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                OutlinedButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Cancel")
                }
            }

            ForgotPasswordStep.Success -> {
                Text(
                    text = "Your password has been updated. Sign in with your new password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthMutedColor,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacing.lg))
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Back to Sign in",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
