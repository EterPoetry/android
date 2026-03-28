package com.nestorian87.eter.ui.screens.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Notifications",
        description = "This screen will show in-app notifications with deep links into posts and comments.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun NotificationsScreenPreview() {
    EterPreview {
        NotificationsScreen()
    }
}
