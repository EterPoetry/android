package com.nestorian87.eter.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Login",
        description = "This screen will host the email sign-in flow and route authenticated users into the feed.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun LoginScreenPreview() {
    EterPreview {
        LoginScreen()
    }
}
