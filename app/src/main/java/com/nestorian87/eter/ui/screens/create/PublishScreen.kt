package com.nestorian87.eter.ui.screens.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun PublishScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Publish",
        description = "This screen will collect post metadata, text, categories, and publishing permissions.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun PublishScreenPreview() {
    EterPreview {
        PublishScreen()
    }
}
