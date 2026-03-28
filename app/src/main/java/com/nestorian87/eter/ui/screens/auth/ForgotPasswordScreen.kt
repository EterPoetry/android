package com.nestorian87.eter.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Forgot Password",
        description = "This screen will start the password reset flow via email.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun ForgotPasswordScreenPreview() {
    EterPreview {
        ForgotPasswordScreen()
    }
}
