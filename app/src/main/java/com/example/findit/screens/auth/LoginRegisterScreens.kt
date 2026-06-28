package com.example.findit.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.findit.ui.components.AuthCardColor
import com.example.findit.ui.components.AuthFieldBorderColor
import com.example.findit.ui.components.AuthMutedColor
import com.example.findit.ui.components.AuthShell
import com.example.findit.ui.components.AuthTextColor
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.AuthPreferences
import com.example.findit.util.PasswordUtils

@Composable
fun LoginScreen(
    authPreferences: AuthPreferences,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthShell(
        title = "Sign in",
        subtitle = "Welcome back to FindIt",
        contentTopPadding = 276.dp,
        footerLink = "Don't have an account? Create one" to onNavigateToRegister
    ) {
        AuthTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        AuthTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = "Password",
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            }
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() ->
                        error = "Please enter your email and password."
                    !PasswordUtils.isValidEmail(email) ->
                        error = "Please enter a valid email address."
                    else -> when (val result = authPreferences.login(email, password)) {
                        is AuthPreferences.AuthResult.Success -> onLoginSuccess()
                        is AuthPreferences.AuthResult.Error -> error = result.message
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Sign In",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = "or continue with",
            style = MaterialTheme.typography.bodySmall,
            color = AuthMutedColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            SocialSignInButton(
                label = "Gmail",
                mark = "G",
                color = Color(0xFFDB4437),
                onClick = {
                    error = "Gmail login needs Google OAuth setup first."
                },
                modifier = Modifier.weight(1f)
            )
            SocialSignInButton(
                label = "Facebook",
                mark = "f",
                color = Color(0xFF1877F2),
                onClick = {
                    error = "Facebook login needs app ID setup first."
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RegisterScreen(
    authPreferences: AuthPreferences,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthShell(
        title = "Create account",
        subtitle = "Secure your FindIt inventory",
        scrollable = true,
        contentTopPadding = 304.dp,
        footerLink = "Already have an account? Sign in" to onNavigateToLogin
    ) {
        AuthSectionLabel("Account details")
        AuthTextField(value = fullName, onValueChange = { fullName = it; error = null }, label = "Full name")
        AuthTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        AuthTextField(
            value = phone,
            onValueChange = { phone = it; error = null },
            label = "Mobile number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(Modifier.height(Spacing.xs))
        AuthSectionLabel("Security")
        AuthTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = "Password",
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
        TermsCheckbox(
            checked = acceptedTerms,
            onCheckedChange = { acceptedTerms = it; error = null }
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Button(
            onClick = {
                when {
                    fullName.isBlank() -> error = "Full name is required."
                    !PasswordUtils.isValidEmail(email) -> error = "Enter a valid email address."
                    phone.length < 10 -> error = "Enter a valid mobile number."
                    !PasswordUtils.isStrongEnough(password) ->
                        error = "Password must be at least 8 characters with letters and numbers."
                    password != confirmPassword -> error = "Passwords do not match."
                    !acceptedTerms -> error = "Please accept the Terms and Privacy Policy."
                    else -> when (val result = authPreferences.register(fullName, email, phone, password)) {
                        is AuthPreferences.AuthResult.Success -> onRegisterSuccess()
                        is AuthPreferences.AuthResult.Error -> error = result.message
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Create Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth(),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AuthTextColor,
            unfocusedTextColor = AuthTextColor,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = AuthMutedColor,
            focusedContainerColor = AuthCardColor,
            unfocusedContainerColor = AuthCardColor,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = AuthFieldBorderColor,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTrailingIconColor = AuthMutedColor,
            unfocusedTrailingIconColor = AuthMutedColor
        )
    )
}

@Composable
private fun AuthSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = AuthTextColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs)
    )
}

@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = "I agree to the Terms of Service and Privacy Policy",
            style = MaterialTheme.typography.bodySmall,
            color = AuthTextColor,
            modifier = Modifier.padding(start = Spacing.xs)
        )
    }
}

@Composable
private fun SocialSignInButton(
    label: String,
    mark: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AuthFieldBorderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AuthCardColor,
            contentColor = AuthTextColor
        )
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mark,
                color = color,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = Spacing.xs)
        )
    }
}
