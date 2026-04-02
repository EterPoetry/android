package com.nestorian87.eter.ui.screens.auth.emailConfirmation

import com.nestorian87.eter.ui.screens.auth.AuthUiMessage

data class EmailConfirmationUiState(
    val email: String = "",
    val code: String = "",
    val remainingMs: Long? = null,
    val hasRequestedCode: Boolean = false,
    val isInitializing: Boolean = true,
    val isRequestingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val errorMessage: AuthUiMessage? = null,
)
