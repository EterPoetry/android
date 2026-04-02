package com.nestorian87.eter.ui.screens.auth

import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.AuthException

fun AuthUiMessage.toMessageResId(): Int = when (this) {
    AuthUiMessage.Unexpected -> R.string.auth_unexpected_error
    is AuthUiMessage.Validation -> when (this) {
        AuthUiMessage.Validation.FILL_CREDENTIALS -> R.string.auth_fill_credentials_error
        AuthUiMessage.Validation.REGISTER_FILL_FIELDS -> R.string.auth_register_fill_fields_error
        AuthUiMessage.Validation.INVALID_EMAIL -> R.string.auth_invalid_email_error
        AuthUiMessage.Validation.PASSWORD_TOO_SHORT -> R.string.auth_password_too_short_error
        AuthUiMessage.Validation.REGISTER_PASSWORD_MISMATCH ->
            R.string.auth_register_password_mismatch_error
    }

    is AuthUiMessage.ReasonMessage -> reason.toMessageResId()
}

fun AuthException.Reason.toMessageResId(): Int = when (this) {
    AuthException.Reason.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials_error
    AuthException.Reason.INVALID_REGISTRATION_DATA -> R.string.auth_register_invalid_data_error
    AuthException.Reason.INVALID_VERIFICATION_CODE -> R.string.auth_email_verification_invalid_code
    AuthException.Reason.EMAIL_VERIFICATION_RATE_LIMIT ->
        R.string.auth_email_verification_rate_limit_error
    AuthException.Reason.EMAIL_ALREADY_EXISTS -> R.string.auth_register_email_exists_error
    AuthException.Reason.NETWORK -> R.string.auth_network_error
    AuthException.Reason.UNKNOWN -> R.string.auth_unexpected_error
}
