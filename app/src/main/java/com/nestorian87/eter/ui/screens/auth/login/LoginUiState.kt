package com.nestorian87.eter.ui.screens.auth.login

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    @param:StringRes val errorMessageResId: Int? = null,
)
