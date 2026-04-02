package com.nestorian87.eter.ui.screens.auth

import com.nestorian87.eter.domain.model.AuthException

sealed interface AuthUiMessage {
    data object GoogleAuthFailed : AuthUiMessage

    enum class Validation : AuthUiMessage {
        FILL_CREDENTIALS,
        REGISTER_FILL_FIELDS,
        FORGOT_PASSWORD_FILL_FIELD,
        INVALID_EMAIL,
        PASSWORD_TOO_SHORT,
        REGISTER_PASSWORD_MISMATCH,
    }

    data class ReasonMessage(
        val reason: AuthException.Reason,
    ) : AuthUiMessage

    data object Unexpected : AuthUiMessage
}

fun Throwable.toAuthUiMessage(): AuthUiMessage = when (this) {
    is AuthException -> AuthUiMessage.ReasonMessage(reason)
    else -> AuthUiMessage.Unexpected
}
