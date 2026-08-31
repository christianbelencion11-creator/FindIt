package com.example.iremember.auth

import com.example.iremember.data.repository.BankCardRepository
import com.example.iremember.data.repository.ItemRepository
import com.example.iremember.data.repository.NoteRepository
import com.example.iremember.util.AuthPreferences
import com.example.iremember.util.PasswordUtils

/**
 * Local, offline account backend. Username/password accounts live on-device in
 * [LocalAccountStore]; the signed-in session is cached in [AuthPreferences]. There is no
 * Firebase or network dependency, so create/sign-in/change-password all complete instantly.
 */
class AccountRepository(
    private val authPreferences: AuthPreferences,
    private val store: LocalAccountStore,
    private val itemRepository: ItemRepository? = null,
    private val noteRepository: NoteRepository? = null,
    private val bankCardRepository: BankCardRepository? = null
) {
    /** Uid of the currently cached session, or null when signed out. */
    val currentUid: String?
        get() = authPreferences.getFirebaseUid().ifBlank { null }

    fun isSignedIn(): Boolean =
        authPreferences.isLoggedIn() && authPreferences.getFirebaseUid().isNotBlank()

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
        if (store.exists(normalized)) {
            return AuthResult.Error("This username is already taken. Sign in instead.")
        }

        val record = store.create(normalized, emailLower, password, secret)
        cacheSession(record.uid, normalized, record.email)
        return AuthResult.Success(
            SignedInUser(record.uid, normalized, record.email, null, PROVIDER_USERNAME)
        )
    }

    suspend fun signInWithUsername(username: String, password: String): AuthResult {
        val normalized = UsernameAuth.normalizeUsername(username)
        if (normalized.isBlank() || password.isBlank()) {
            return AuthResult.Error("Please enter your username and password.")
        }
        val record = store.get(normalized)
            ?: return AuthResult.Error("No account found for that username. Create one first.")
        if (!store.verifyPassword(normalized, password)) {
            return AuthResult.Error("Incorrect username or password.")
        }
        cacheSession(record.uid, normalized, record.email)
        return AuthResult.Success(
            SignedInUser(record.uid, normalized, record.email, null, PROVIDER_USERNAME)
        )
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
        val username = authPreferences.getUsername()
        val record = store.get(username)
            ?: return AuthResult.Error("Unable to verify your account. Sign out and sign in again.")
        if (!store.verifyPassword(username, currentPassword)) {
            return AuthResult.Error("Current password is incorrect.")
        }
        store.updatePassword(username, newPassword)
        return AuthResult.Success(
            SignedInUser(record.uid, record.username, record.email, null, PROVIDER_USERNAME)
        )
    }

    fun signOut() {
        authPreferences.clearSessionData()
        itemRepository?.setOwnerUid("")
        noteRepository?.setOwnerUid("")
        bankCardRepository?.setOwnerUid("")
    }

    private fun cacheSession(uid: String, username: String, email: String) {
        authPreferences.syncFromFirebaseUser(
            fullName = username,
            email = email,
            phone = "",
            provider = PROVIDER_USERNAME,
            firebaseUid = uid,
            username = username
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

    companion object {
        const val PROVIDER_USERNAME = "username"
    }
}
