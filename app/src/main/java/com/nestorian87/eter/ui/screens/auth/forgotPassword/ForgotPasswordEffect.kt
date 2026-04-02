package com.nestorian87.eter.ui.screens.auth.forgotPassword

sealed interface ForgotPasswordEffect {
    data class PasswordResetSent(
        val email: String,
    ) : ForgotPasswordEffect
}
