package com.example.iremember.screens.auth

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
import com.example.iremember.auth.AccountRepository
import com.example.iremember.ui.components.AuthErrorText
import com.example.iremember.ui.components.AuthMutedColor
import com.example.iremember.ui.components.AuthShell
import com.example.iremember.ui.components.AuthTextColor
import com.example.iremember.ui.theme.Spacing
import com.example.iremember.util.PasswordUtils
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    authRepository: AccountRepository,
    username: String,
    onCancel: () -> Unit,
    onSuccess: () -> Unit = onCancel
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val displayUser = username.trim().ifBlank { "your account" }

    AuthShell(
        title = if (success) "Password updated" else "Re-Create Password",
        subtitle = if (success) {
            "Your password has been changed"
        } else {
            "Please enter your New Password"
        },
        scrollable = true,
        footerLink = if (success) null else "Back to Profile" to onCancel
    ) {
        if (success) {
            Text(
                text = "You can use your new password the next time you sign in.",
                style = MaterialTheme.typography.bodyMedium,
                color = AuthMutedColor,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = onSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Back to Profile",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            return@AuthShell
        }

        Text(
            text = "Account details:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AuthTextColor,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = displayUser,
            style = MaterialTheme.typography.bodyLarge,
            color = AuthMutedColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.md)
        )

        AuthSectionLabel("Security")
        AuthTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; error = null },
            label = "Current password",
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
        AuthTextField(
            value = newPassword,
            onValueChange = { newPassword = it; error = null },
            label = "New Password",
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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

        error?.let { AuthErrorText(it, modifier = Modifier.padding(top = Spacing.xs)) }
        Spacer(Modifier.height(Spacing.sm))

        Button(
            onClick = {
                when {
                    currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                        error = "Please fill in all password fields."
                    !PasswordUtils.isStrongEnough(newPassword) ->
                        error = "Password must be at least 8 characters with letters and numbers."
                    newPassword != confirmPassword ->
                        error = "Passwords do not match."
                    else -> {
                        isLoading = true
                        error = null
                        scope.launch {
                            when (
                                val result = authRepository.changePassword(
                                    currentPassword,
                                    newPassword
                                )
                            ) {
                                is AccountRepository.AuthResult.Success -> success = true
                                is AccountRepository.AuthResult.Error -> error = result.message
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
            onClick = onCancel,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("Cancel")
        }
    }
}
