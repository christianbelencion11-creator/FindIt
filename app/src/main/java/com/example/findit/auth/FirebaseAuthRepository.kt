package com.example.findit.auth

import com.example.findit.data.repository.ItemRepository
import com.example.findit.util.AuthPreferences
import com.example.findit.util.PasswordUtils
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseAuthRepository(
    private val authPreferences: AuthPreferences,
    private val itemRepository: ItemRepository? = null,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun createAccountWithUsername(
        username: String,
        email: String,
        password: String,
        secret: String
    ): AuthResult {
        val normalized = UsernameAuth.normalizeUsername(username)
        val emailLower = email.trim().lowercase()
        if (!UsernameAuth.isValidUsername(normalized)) {
            return AuthResult.Error(
                "Username must be 3–24 characters (letters, numbers, underscore)."
            )
        }
        if (!PasswordUtils.isValidEmail(emailLower)) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        if (!PasswordUtils.isStrongEnough(password)) {
            return AuthResult.Error(
                "Password must be at least 8 characters with letters and numbers."
            )
        }
        if (secret.isBlank()) {
            return AuthResult.Error("Secret details are required for account recovery.")
        }

        val authEmail = UsernameAuth.syntheticEmail(normalized)
        var createdUser: FirebaseUser? = null
        return try {
            withTimeout(CREATE_ACCOUNT_TIMEOUT_MS) {
                withTimeoutOrNull(5_000) {
                    firestore.enableNetwork().await()
                }

                val result = withTimeout(AUTH_OP_TIMEOUT_MS) {
                    firebaseAuth.createUserWithEmailAndPassword(authEmail, password).await()
                }
                val user = result.user
                    ?: return@withTimeout AuthResult.Error("Registration failed. Please try again.")
                createdUser = user

                withTimeout(AUTH_OP_TIMEOUT_MS) {
                    user.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(normalized)
                            .build()
                    ).await()
                }

                try {
                    withTimeout(FIRESTORE_WRITE_TIMEOUT_MS) {
                        firestore.collection(EMAIL_INDEX_COLLECTION)
                            .document(emailLower)
                            .set(
                                hashMapOf(
                                    "username" to normalized,
                                    "firebaseUid" to user.uid,
                                    "email" to emailLower
                                )
                            )
                            .await()
                    }
                } catch (e: TimeoutCancellationException) {
                    safeDeleteUser(user)
                    return@withTimeout AuthResult.Error(CONNECTION_TIMEOUT_MESSAGE)
                } catch (e: Exception) {
                    safeDeleteUser(user)
                    return@withTimeout AuthResult.Error(mapFirestoreWriteError(e, kind = "email"))
                }

                val passwordSalt = PasswordUtils.generateSalt()
                val secretSalt = PasswordUtils.generateSalt()
                val doc = hashMapOf(
                    "username" to normalized,
                    "usernameLower" to normalized,
                    "email" to emailLower,
                    "emailLower" to emailLower,
                    "firebaseUid" to user.uid,
                    "passwordSalt" to passwordSalt,
                    "passwordHash" to PasswordUtils.hashPassword(password, passwordSalt),
                    "secretSalt" to secretSalt,
                    "secretHash" to PasswordUtils.hashPassword(secret.trim(), secretSalt),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    withTimeout(FIRESTORE_WRITE_TIMEOUT_MS) {
                        firestore.collection(USERS_COLLECTION)
                            .document(normalized)
                            .set(doc)
                            .await()
                    }
                } catch (e: TimeoutCancellationException) {
                    withTimeoutOrNull(FIRESTORE_WRITE_TIMEOUT_MS) {
                        firestore.collection(EMAIL_INDEX_COLLECTION).document(emailLower).delete().await()
                    }
                    safeDeleteUser(user)
                    return@withTimeout AuthResult.Error(CONNECTION_TIMEOUT_MESSAGE)
                } catch (e: Exception) {
                    withTimeoutOrNull(FIRESTORE_WRITE_TIMEOUT_MS) {
                        firestore.collection(EMAIL_INDEX_COLLECTION).document(emailLower).delete().await()
                    }
                    safeDeleteUser(user)
                    return@withTimeout AuthResult.Error(mapFirestoreWriteError(e, kind = "username"))
                }

                cacheUser(
                    user = user,
                    phone = "",
                    provider = PROVIDER_USERNAME,
                    username = normalized,
                    contactEmail = emailLower
                )
                AuthResult.Success(
                    toSignedInUser(user, PROVIDER_USERNAME, username = normalized, contactEmail = emailLower)
                )
            }
        } catch (_: TimeoutCancellationException) {
            createdUser?.let { safeDeleteUser(it) }
                ?: safeDeleteUser(firebaseAuth.currentUser)
            AuthResult.Error(CONNECTION_TIMEOUT_MESSAGE)
        } catch (_: FirebaseAuthUserCollisionException) {
            AuthResult.Error("This username is already taken. Sign in instead.")
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            createdUser?.let { safeDeleteUser(it) }
                ?: run {
                    withTimeoutOrNull(DELETE_USER_TIMEOUT_MS) {
                        firebaseAuth.currentUser?.delete()?.await()
                    }
                }
            val msg = e.message.orEmpty()
            if (
                msg.contains("client is offline", ignoreCase = true) ||
                msg.contains("Unable to resolve host", ignoreCase = true) ||
                msg.contains("network", ignoreCase = true) ||
                msg.contains("timed out", ignoreCase = true)
            ) {
                AuthResult.Error("No internet connection. Check Wi‑Fi/data and try again.")
            } else {
                AuthResult.Error(e.message ?: "Registration failed. Please try again.")
            }
        }
    }

    private suspend fun safeDeleteUser(user: FirebaseUser?) {
        if (user == null) return
        withTimeoutOrNull(DELETE_USER_TIMEOUT_MS) {
            user.delete().await()
        }
    }

    suspend fun signInWithUsername(username: String, password: String): AuthResult {
        val normalized = UsernameAuth.normalizeUsername(username)
        if (normalized.isBlank() || password.isBlank()) {
            return AuthResult.Error("Please enter your username and password.")
        }
        return try {
            val email = UsernameAuth.syntheticEmail(normalized)
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")
            cacheUser(user, phone = "", provider = PROVIDER_USERNAME, username = normalized)
            AuthResult.Success(toSignedInUser(user, PROVIDER_USERNAME, username = normalized))
        } catch (_: Exception) {
            AuthResult.Error("Incorrect username or password.")
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult {
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            return AuthResult.Error("Please fill in all password fields.")
        }
        if (!PasswordUtils.isStrongEnough(newPassword)) {
            return AuthResult.Error(
                "New password must be at least 8 characters with letters and numbers."
            )
        }
        if (currentPassword == newPassword) {
            return AuthResult.Error("New password must be different from the current password.")
        }
        val user = firebaseAuth.currentUser
            ?: return AuthResult.Error("Please sign in again to change your password.")
        val username = authPreferences.getUsername().ifBlank {
            UsernameAuth.usernameFromSyntheticEmail(user.email.orEmpty()).orEmpty()
        }
        if (username.isBlank()) {
            return AuthResult.Error("Unable to verify your account. Sign out and sign in again.")
        }
        val authEmail = UsernameAuth.syntheticEmail(username)
        return try {
            withTimeout(AUTH_OP_TIMEOUT_MS) {
                val credential = EmailAuthProvider.getCredential(authEmail, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
            }
            AuthResult.Success(toSignedInUser(user, PROVIDER_USERNAME, username = username))
        } catch (_: TimeoutCancellationException) {
            AuthResult.Error("Connection timed out. Check your internet and try again.")
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Current password is incorrect.")
        } catch (_: FirebaseAuthRecentLoginRequiredException) {
            AuthResult.Error("For security, sign out and sign in again, then change your password.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not change password. Please try again.")
        }
    }

    @Deprecated("Use createAccountWithUsername")
    suspend fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(
                email.trim().lowercase(),
                password
            ).await()
            val user = result.user ?: return AuthResult.Error("Registration failed. Please try again.")
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
            ).await()
            cacheUser(user, phone.trim(), PROVIDER_EMAIL)
            AuthResult.Success(toSignedInUser(user, PROVIDER_EMAIL))
        } catch (_: FirebaseAuthUserCollisionException) {
            AuthResult.Error("This email is already registered. Sign in instead.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed. Please try again.")
        }
    }

    @Deprecated("Use signInWithUsername")
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(
                email.trim().lowercase(),
                password
            ).await()
            val user = result.user ?: return AuthResult.Error("Sign in failed. Please try again.")
            cacheUser(user, authPreferences.getPhone(), PROVIDER_EMAIL)
            AuthResult.Success(toSignedInUser(user, PROVIDER_EMAIL))
        } catch (_: Exception) {
            AuthResult.Error("Incorrect email or password.")
        }
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google sign-in failed. Please try again.")
            cacheUser(user, authPreferences.getPhone(), PROVIDER_GOOGLE)
            AuthResult.Success(toSignedInUser(user, PROVIDER_GOOGLE))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        authPreferences.clearSessionData()
        itemRepository?.setOwnerUid("")
    }

    fun isFirebaseSignedIn(): Boolean = firebaseAuth.currentUser != null

    private fun cacheUser(
        user: FirebaseUser,
        phone: String,
        provider: String,
        username: String? = null,
        contactEmail: String? = null
    ) {
        val resolvedUsername = username
            ?: UsernameAuth.usernameFromSyntheticEmail(user.email.orEmpty())
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@").orEmpty()
        val resolvedEmail = contactEmail?.trim()?.takeIf { it.isNotBlank() }
            ?: authPreferences.getEmail().takeIf {
                it.isNotBlank() && !it.endsWith("@users.iremember.app")
            }
            ?: user.email.orEmpty().takeUnless { it.endsWith("@users.iremember.app") }
            ?: ""
        authPreferences.syncFromFirebaseUser(
            fullName = resolvedUsername.ifBlank { "IRemember User" },
            email = resolvedEmail,
            phone = phone,
            provider = provider,
            firebaseUid = user.uid,
            username = resolvedUsername
        )
    }

    private fun toSignedInUser(
        user: FirebaseUser,
        provider: String,
        username: String? = null,
        contactEmail: String? = null
    ): SignedInUser {
        val resolvedUsername = username
            ?: UsernameAuth.usernameFromSyntheticEmail(user.email.orEmpty())
            ?: user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@").orEmpty()
        val resolvedEmail = contactEmail?.trim()?.takeIf { it.isNotBlank() }
            ?: user.email.orEmpty().takeUnless { it.endsWith("@users.iremember.app") }
            ?: ""
        return SignedInUser(
            uid = user.uid,
            displayName = resolvedUsername.ifBlank { "IRemember User" },
            email = resolvedEmail,
            photoUrl = user.photoUrl?.toString(),
            provider = provider
        )
    }

    data class SignedInUser(
        val uid: String,
        val displayName: String,
        val email: String,
        val photoUrl: String?,
        val provider: String
    )

    sealed class AuthResult {
        data class Success(val user: SignedInUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    private fun mapFirestoreWriteError(e: Exception, kind: String): String {
        val code = (e as? FirebaseFirestoreException)?.code
        val msg = e.message.orEmpty()
        val isOffline = code == FirebaseFirestoreException.Code.UNAVAILABLE ||
            msg.contains("client is offline", ignoreCase = true) ||
            msg.contains("UNAVAILABLE", ignoreCase = true) ||
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("network", ignoreCase = true)
        val isPermission = code == FirebaseFirestoreException.Code.PERMISSION_DENIED ||
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
            msg.contains("permission-denied", ignoreCase = true)
        val isAlreadyExists = code == FirebaseFirestoreException.Code.ALREADY_EXISTS ||
            msg.contains("ALREADY_EXISTS", ignoreCase = true) ||
            msg.contains("already exists", ignoreCase = true)

        return when {
            isOffline ->
                "No internet connection. Check Wi‑Fi/data and try again."
            isAlreadyExists && kind == "email" ->
                "This email is already registered. Sign in instead."
            isAlreadyExists && kind == "username" ->
                "This username is already taken. Sign in instead."
            // Existing emailIndex docs deny overwrite → often surfaces as PERMISSION_DENIED
            isPermission && kind == "email" ->
                "This email is already registered (or Firestore rules are not deployed). " +
                    "Try Sign in, or deploy firestore.rules then try again."
            isPermission && kind == "username" ->
                "This username is already taken (or Firestore rules are not deployed). " +
                    "Try Sign in, or deploy firestore.rules then try again."
            else -> e.message ?: "Registration failed. Please try again."
        }
    }

    companion object {
        const val PROVIDER_EMAIL = "email"
        const val PROVIDER_GOOGLE = "google"
        const val PROVIDER_USERNAME = "username"
        const val USERS_COLLECTION = "users"
        const val EMAIL_INDEX_COLLECTION = "emailIndex"

        private const val CREATE_ACCOUNT_TIMEOUT_MS = 25_000L
        private const val AUTH_OP_TIMEOUT_MS = 15_000L
        private const val FIRESTORE_WRITE_TIMEOUT_MS = 12_000L
        private const val DELETE_USER_TIMEOUT_MS = 5_000L
        private const val CONNECTION_TIMEOUT_MESSAGE =
            "Connection timed out. Check your internet and try again."
    }
}
