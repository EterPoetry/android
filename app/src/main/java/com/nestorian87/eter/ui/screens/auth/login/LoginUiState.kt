package com.nestorian87.eter.ui.screens.auth.login

import com.nestorian87.eter.ui.screens.auth.AuthSubmissionType
import com.nestorian87.eter.ui.screens.auth.AuthUiMessage

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val activeSubmission: AuthSubmissionType? = null,
    val errorMessage: AuthUiMessage? = null,
)
