package com.nestorian87.eter.ui.screens.auth.register

sealed interface RegisterEffect {
    data object NavigateToMain : RegisterEffect
}
