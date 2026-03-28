package com.nestorian87.eter.ui.screens.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun SubscriptionsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Subscriptions",
        description = "This screen will show posts from authors the user follows.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun SubscriptionsScreenPreview() {
    EterPreview {
        SubscriptionsScreen()
    }
}
