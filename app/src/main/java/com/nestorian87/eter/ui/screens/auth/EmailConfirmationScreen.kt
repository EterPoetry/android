package com.nestorian87.eter.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun EmailConfirmationScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Confirm Email",
        description = "This screen will guide the user through verifying their email address.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun EmailConfirmationScreenPreview() {
    EterPreview {
        EmailConfirmationScreen()
    }
}
