package com.nestorian87.eter.ui.app

data class AppSessionUiState(
    val isCheckingSession: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isEmailVerified: Boolean = false,
)
