package com.nestorian87.eter.ui.screens.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun RecordAudioScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Record Audio",
        description = "This screen will handle microphone capture, waveform preview, and audio draft controls.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun RecordAudioScreenPreview() {
    EterPreview {
        RecordAudioScreen()
    }
}
