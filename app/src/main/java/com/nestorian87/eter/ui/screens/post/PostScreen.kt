package com.nestorian87.eter.ui.screens.post

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun PostScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Post",
        description = "This screen will combine playback, post text, reactions, and threaded comments.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun PostScreenPreview() {
    EterPreview {
        PostScreen()
    }
}
