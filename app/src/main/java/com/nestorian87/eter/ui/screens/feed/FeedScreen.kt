package com.nestorian87.eter.ui.screens.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun FeedScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Feed",
        description = "This screen will show the main feed for fresh and popular audio posts.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun FeedScreenPreview() {
    EterPreview {
        FeedScreen()
    }
}
