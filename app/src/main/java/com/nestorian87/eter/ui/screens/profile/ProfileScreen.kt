package com.nestorian87.eter.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Profile",
        description = "This screen will cover both the current user profile and public author profiles.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun ProfileScreenPreview() {
    EterPreview {
        ProfileScreen()
    }
}
