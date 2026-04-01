package com.nestorian87.eter.ui.screens.auth.login

sealed interface LoginEffect {
    data object NavigateToMain : LoginEffect
}
