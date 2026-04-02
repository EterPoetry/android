package com.nestorian87.eter.ui.screens.auth.google

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch

private const val GOOGLE_AUTH_LOG_TAG = "GoogleAuth"

@Composable
fun rememberGoogleIdTokenRequester(
    serverClientId: String,
    onTokenReceived: (String) -> Unit,
    onFailure: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return remember(context, serverClientId, onTokenReceived, onFailure) {
        {
            val activity = context.findActivity()
            if (activity == null || serverClientId.isBlank()) {
                Log.d(
                    GOOGLE_AUTH_LOG_TAG,
                    "Google sign-in unavailable: activity=${activity != null}, hasServerClientId=${serverClientId.isNotBlank()}",
                )
                onFailure()
                return@remember
            }

            scope.launch {
                when (val result = requestGoogleIdToken(activity, serverClientId)) {
                    is GoogleIdTokenResult.Success -> onTokenReceived(result.idToken)
                    GoogleIdTokenResult.Cancelled -> Unit
                    GoogleIdTokenResult.Failure -> onFailure()
                }
            }
        }
    }
}

private suspend fun requestGoogleIdToken(
    activity: ComponentActivity,
    serverClientId: String,
): GoogleIdTokenResult {
    val credentialManager = CredentialManager.create(activity)
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(
            GetSignInWithGoogleOption.Builder(serverClientId).build(),
        )
        .build()

    return try {
        val result = credentialManager.getCredential(
            context = activity,
            request = request,
        )
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Log.d(GOOGLE_AUTH_LOG_TAG, "Google ID token credential received successfully")
            GoogleIdTokenResult.Success(
                idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken,
            )
        } else {
            Log.d(
                GOOGLE_AUTH_LOG_TAG,
                "Unexpected credential type received: ${credential::class.java.simpleName}",
            )
            GoogleIdTokenResult.Failure
        }
    } catch (_: GetCredentialCancellationException) {
        Log.d(GOOGLE_AUTH_LOG_TAG, "Google sign-in cancelled by user")
        GoogleIdTokenResult.Cancelled
    } catch (error: GetCredentialException) {
        Log.d(
            GOOGLE_AUTH_LOG_TAG,
            "Google sign-in failed during credential request: ${error::class.java.simpleName}: ${error.message}",
        )
        GoogleIdTokenResult.Failure
    }
}

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private sealed interface GoogleIdTokenResult {
    data class Success(
        val idToken: String,
    ) : GoogleIdTokenResult

    data object Cancelled : GoogleIdTokenResult

    data object Failure : GoogleIdTokenResult
}
