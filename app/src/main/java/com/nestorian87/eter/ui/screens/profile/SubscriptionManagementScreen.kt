package com.nestorian87.eter.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun SubscriptionManagementScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Manage Subscription",
        description = "This screen will cover premium plan status, renewal details, and upgrade actions.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun SubscriptionManagementScreenPreview() {
    EterPreview {
        SubscriptionManagementScreen()
    }
}
