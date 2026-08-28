package com.example.findit.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.findit.auth.AccountRepository
import com.example.findit.auth.UsernameAuth
import com.example.findit.ui.components.AuthCardColor
import com.example.findit.ui.components.AuthErrorText
import com.example.findit.ui.components.AuthFieldBorderColor
import com.example.findit.ui.components.AuthMutedColor
import com.example.findit.ui.components.AuthShell
import com.example.findit.ui.components.AuthTextColor
import com.example.findit.ui.theme.Spacing
import com.example.findit.util.AuthPreferences
import com.example.findit.util.PasswordUtils
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AccountRepository,
    authPreferences: AuthPreferences,
    onLoginSuccess: (AccountRepository.SignedInUser) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: (username: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AuthShell(
        title = "Sign in",
        subtitle = "Welcome back to IRemember Sign-in Page",
        scrollable = true,
        footerLink = "Don't have an account? Create one" to onNavigateToRegister
    ) {
        AuthTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = "Username"
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
        TextButton(
            onClick = { onNavigateToForgotPassword(username.trim()) },
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        )
        error?.let { AuthErrorText(it) }
        Spacer(Modifier.height(Spacing.sm))
        Button(
            onClick = {
                if (isLoading) return@Button
                when {
                    username.isBlank() || password.isBlank() ->
                        error = "Please enter your username and password."
                    UsernameAuth.looksLikeEmail(username) ->
                        error = "Sign in with your username (not email)."
                    else -> {
                        isLoading = true
                        error = null
                        scope.launch {
                            try {
                                when (val result = authRepository.signInWithUsername(username, password)) {
                                    is AccountRepository.AuthResult.Success ->
                                        onLoginSuccess(result.user)
                                    is AccountRepository.AuthResult.Error -> error = result.message
                                }
                            } finally {
                                isLoading = false
                            }
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = Spacing.sm)
                        .size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
            Text(
                if (isLoading) "Signing in…" else "Sign In",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun RegisterScreen(
    authRepository: AccountRepository,
    authPreferences: AuthPreferences,
    onRegisterSuccess: (AccountRepository.SignedInUser) -> Unit,
    onNavigateToLogin: () -> Unit,
    onPrivacyPolicyClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var secretDetails by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showExistingAccountBanner by remember { mutableStateOf(false) }
    var pendingSuccessUser by remember {
        mutableStateOf<AccountRepository.SignedInUser?>(null)
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        showExistingAccountBanner = authRepository.isSignedIn() ||
            (authPreferences.isRegistered() && authPreferences.getUsername().isNotBlank())
    }

    if (pendingSuccessUser != null) {
        AccountCreatedSuccessDialog(
            username = pendingSuccessUser!!.displayName,
            onContinue = {
                val user = pendingSuccessUser!!
                pendingSuccessUser = null
                onRegisterSuccess(user)
            }
        )
    }

    AuthShell(
        title = "Create New Account",
        subtitle = "Please enter your details and ensure your password",
        scrollable = true,
        contentTopPadding = 304.dp,
        footerLink = "Already have an account? Sign in" to onNavigateToLogin,
        stickyBottomBar = {
            error?.let {
                AuthErrorText(
                    it,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                if (it.contains("already taken", ignoreCase = true) ||
                    it.contains("already registered", ignoreCase = true)
                ) {
                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    ) {
                        Text("Go to Sign in")
                    }
                }
            }
            Button(
                onClick = {
                    if (isLoading) return@Button
                    when {
                        !UsernameAuth.isValidUsername(username) ->
                            error = "Username must be 3–24 characters (letters, numbers, underscore)."
                        !PasswordUtils.isValidEmail(email) ->
                            error = "Please enter a valid email address."
                        !PasswordUtils.isStrongEnough(password) ->
                            error = "Password must be at least 8 characters with letters and numbers."
                        password != confirmPassword -> error = "Passwords do not match."
                        secretDetails.isBlank() -> error = "Secret details are required."
                        !acceptedTerms -> error = "Please accept the Terms and Privacy Policy."
                        else -> {
                            isLoading = true
                            error = null
                            scope.launch {
                                try {
                                    when (
                                        val result = authRepository.createAccountWithUsername(
                                            username = username,
                                            email = email,
                                            password = password,
                                            secret = secretDetails
                                        )
                                    ) {
                                        is AccountRepository.AuthResult.Success ->
                                            pendingSuccessUser = result.user
                                        is AccountRepository.AuthResult.Error ->
                                            error = result.message
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "Registration failed. Please try again."
                                } finally {
                                    isLoading = false
                                }
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    // Keep solid green while loading — default disabled style looks like the button vanished.
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = Spacing.sm)
                            .size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    if (isLoading) "Creating account…" else "Create Account",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) {
        if (showExistingAccountBanner) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onNavigateToLogin)
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "An account already exists — tap here to Sign in instead.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Spacing.sm))
        }
        AuthSectionLabel("Account details")
        AuthTextField(
            value = username,
            onValueChange = { username = it; error = null },
            label = "Username"
        )
        AuthTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email address",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
        Spacer(Modifier.height(Spacing.xs))
        AuthSectionLabel("Additional Security (in case password is forgotten)")
        AuthTextField(
            value = secretDetails,
            onValueChange = { secretDetails = it; error = null },
            label = "Add Secret Details"
        )
        TermsCheckbox(
            checked = acceptedTerms,
            onCheckedChange = { acceptedTerms = it; error = null },
            onPrivacyPolicyClick = onPrivacyPolicyClick
        )
    }
}

@Composable
private fun AccountCreatedSuccessDialog(
    username: String,
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AuthCardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = "Account created successfully",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AuthTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Welcome, $username! Next, set your MPIN to secure IRemember.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthMutedColor,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.xxl))
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
        modifier = modifier.fillMaxWidth(),
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
fun AuthSectionLabel(text: String) {
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
    onCheckedChange: (Boolean) -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val annotated = buildAnnotatedString {
        append("I agree to the Terms of Service and ")
        pushStringAnnotation(tag = "privacy", annotation = "privacy")
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
    }

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
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.bodySmall.copy(color = AuthTextColor),
            modifier = Modifier.padding(start = Spacing.xs),
            onClick = { offset ->
                annotated.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                    .firstOrNull()
                    ?.let { onPrivacyPolicyClick() }
            }
        )
    }
}
