package com.example.findit.auth

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

class SecretRecoveryRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    suspend fun verifySecret(username: String, secret: String): Result {
        return call(
            "verifySecret",
            mapOf(
                "username" to UsernameAuth.normalizeUsername(username),
                "secret" to secret
            )
        )
    }

    suspend fun resetPasswordWithSecret(
        username: String,
        secret: String,
        newPassword: String,
        newSecret: String?
    ): Result {
        val data = mutableMapOf(
            "username" to UsernameAuth.normalizeUsername(username),
            "secret" to secret,
            "newPassword" to newPassword
        )
        if (!newSecret.isNullOrBlank()) {
            data["newSecret"] = newSecret
        }
        return call("resetPasswordWithSecret", data)
    }

    private suspend fun call(functionName: String, data: Map<String, String>): Result {
        return try {
            functions.getHttpsCallable(functionName).call(data).await()
            Result.Success
        } catch (e: FirebaseFunctionsException) {
            val message = when (e.code) {
                FirebaseFunctionsException.Code.NOT_FOUND ->
                    e.message ?: "Account not found. Check your username."
                FirebaseFunctionsException.Code.UNAVAILABLE ->
                    "Service unavailable. Check your internet connection."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                    e.message ?: "Incorrect secret details. Please try again."
                FirebaseFunctionsException.Code.PERMISSION_DENIED ->
                    e.message ?: "Incorrect secret details. Please try again."
                else -> e.message ?: "Could not complete the request. Please try again."
            }
            Result.Error(message)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Could not complete the request. Please try again.")
        }
    }

    sealed class Result {
        data object Success : Result()
        data class Error(val message: String) : Result()
    }
}
