package com.nestorian87.eter.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Settings",
        description = "This screen will group account, playback, notification, and privacy preferences.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun SettingsScreenPreview() {
    EterPreview {
        SettingsScreen()
    }
}
