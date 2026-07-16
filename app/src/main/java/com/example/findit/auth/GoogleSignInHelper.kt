package com.example.findit.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleSignInHelper {

    private fun getWebClientId(activity: Activity): String {
        val resId = activity.resources.getIdentifier(
            "default_web_client_id",
            "string",
            activity.packageName
        )
        if (resId == 0) return ""
        return activity.getString(resId)
    }

    suspend fun getGoogleIdToken(activity: Activity): Result<String> = withContext(Dispatchers.Main) {
        try {
            val webClientId = getWebClientId(activity)
            if (webClientId.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Google Sign-In is not configured. Enable Google in Firebase Console, " +
                            "add your debug SHA-1 fingerprint, re-download google-services.json, " +
                            "then rebuild. See FIREBASE_SETUP.md."
                    )
                )
            }
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credentialManager = CredentialManager.create(activity)
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleCredential.idToken)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type."))
            }
        } catch (_: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Google sign-in was cancelled."))
        } catch (_: NoCredentialException) {
            Result.failure(IllegalStateException("No Google account available on this device."))
        } catch (e: GetCredentialException) {
            Result.failure(IllegalStateException(e.message ?: "Google sign-in failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
