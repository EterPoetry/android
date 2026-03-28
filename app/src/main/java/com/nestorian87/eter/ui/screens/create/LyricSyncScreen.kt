package com.nestorian87.eter.ui.screens.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun LyricSyncScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Lyric Sync",
        description = "This screen will let authors map text lines to audio timestamps for synced playback.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun LyricSyncScreenPreview() {
    EterPreview {
        LyricSyncScreen()
    }
}
