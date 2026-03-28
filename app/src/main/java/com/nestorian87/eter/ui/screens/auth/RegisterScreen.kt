package com.nestorian87.eter.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun RegisterScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Register",
        description = "This screen will capture account details for a new user registration flow.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun RegisterScreenPreview() {
    EterPreview {
        RegisterScreen()
    }
}
