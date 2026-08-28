package com.example.findit.auth

/**
 * Local secret-based account recovery. Verifies the user's "secret details" against the
 * on-device [LocalAccountStore] and resets the password/secret there. No network or
 * Firebase Functions — the [Result] API is kept so the Forgot Password UI is unchanged.
 */
class SecretRecoveryRepository(
    private val store: LocalAccountStore
) {
    suspend fun verifySecret(username: String, secret: String): Result {
        val normalized = UsernameAuth.normalizeUsername(username)
        if (store.get(normalized) == null) {
            return Result.Error("Account not found. Check your username.")
        }
        return if (store.verifySecret(normalized, secret)) {
            Result.Success
        } else {
            Result.Error("Incorrect secret details. Please try again.")
        }
    }

    suspend fun resetPasswordWithSecret(
        username: String,
        secret: String,
        newPassword: String,
        newSecret: String?
    ): Result {
        val normalized = UsernameAuth.normalizeUsername(username)
        if (store.get(normalized) == null) {
            return Result.Error("Account not found. Check your username.")
        }
        if (!store.verifySecret(normalized, secret)) {
            return Result.Error("Incorrect secret details. Please try again.")
        }
        store.updatePassword(normalized, newPassword)
        if (!newSecret.isNullOrBlank()) {
            store.updateSecret(normalized, newSecret)
        }
        return Result.Success
    }

    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }
}
