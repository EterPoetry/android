package com.nestorian87.eter.ui.screens.auth.register

import com.nestorian87.eter.ui.screens.auth.AuthSubmissionType
import com.nestorian87.eter.ui.screens.auth.AuthUiMessage

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val activeSubmission: AuthSubmissionType? = null,
    val errorMessage: AuthUiMessage? = null,
)
