package com.nestorian87.eter.ui.screens.auth.forgotPassword

import com.nestorian87.eter.ui.screens.auth.AuthUiMessage

data class ForgotPasswordUiState(
    val email: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: AuthUiMessage? = null,
)
