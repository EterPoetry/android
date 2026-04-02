package com.nestorian87.eter.ui.screens.auth.register

import androidx.annotation.StringRes

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    @param:StringRes val errorMessageResId: Int? = null,
)
