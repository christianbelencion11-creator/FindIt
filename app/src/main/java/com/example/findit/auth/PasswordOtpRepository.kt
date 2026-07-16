package com.example.findit.auth

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await

class PasswordOtpRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {

    suspend fun sendPasswordOtp(email: String): OtpResult {
        return callFunction(
            functionName = "sendPasswordOtp",
            data = mapOf("email" to email.trim().lowercase())
        )
    }

    suspend fun verifyOtpAndResetPassword(
        email: String,
        otp: String,
        newPassword: String
    ): OtpResult {
        return callFunction(
            functionName = "verifyOtpAndResetPassword",
            data = mapOf(
                "email" to email.trim().lowercase(),
                "otp" to otp.trim(),
                "newPassword" to newPassword
            )
        )
    }

    private suspend fun callFunction(functionName: String, data: Map<String, String>): OtpResult {
        return try {
            functions
                .getHttpsCallable(functionName)
                .call(data)
                .await()
            OtpResult.Success
        } catch (e: FirebaseFunctionsException) {
            val message = when (e.code) {
                FirebaseFunctionsException.Code.NOT_FOUND ->
                    "OTP service not configured. Deploy Cloud Functions — see FIREBASE_SETUP.md."
                FirebaseFunctionsException.Code.UNAVAILABLE ->
                    "OTP service is unavailable. Check your internet connection and try again."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                    e.message ?: "Invalid request. Check your email and try again."
                FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                    "Request timed out. Please try again."
                else -> e.message ?: "Could not complete the request. Please try again."
            }
            OtpResult.Error(message)
        } catch (e: Exception) {
            OtpResult.Error(
                e.message ?: "OTP service not configured. Deploy Cloud Functions — see FIREBASE_SETUP.md."
            )
        }
    }

    sealed class OtpResult {
        data object Success : OtpResult()
        data class Error(val message: String) : OtpResult()
    }
}
