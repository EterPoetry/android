package com.nestorian87.eter.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun FollowersScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Followers",
        description = "This screen will show both followers and following lists for a profile.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun FollowersScreenPreview() {
    EterPreview {
        FollowersScreen()
    }
}
