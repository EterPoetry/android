package com.nestorian87.eter.ui.screens.create

data class SelectedAudioUiState(
    val displayName: String? = null,
    val durationMs: Long,
    val localFilePath: String,
    val recordedAtEpochMs: Long? = null,
)
