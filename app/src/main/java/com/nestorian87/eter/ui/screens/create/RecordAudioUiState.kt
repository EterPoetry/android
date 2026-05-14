package com.nestorian87.eter.ui.screens.create

data class RecordAudioUiState(
    val sourceMode: RecordAudioSourceMode = RecordAudioSourceMode.RECORD,
    val maxDurationMinutes: Int = 7,
    val isImportingAudio: Boolean = false,
    val isUploadingPost: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val recordedAudio: SelectedAudioUiState? = null,
    val uploadedAudio: SelectedAudioUiState? = null,
    val errorMessage: RecordAudioUiMessage? = null,
) {
    val maxDurationMs: Long
        get() = maxDurationMinutes * 60_000L

    val activeSelectedAudio: SelectedAudioUiState?
        get() = when (sourceMode) {
            RecordAudioSourceMode.RECORD -> recordedAudio
            RecordAudioSourceMode.UPLOAD -> uploadedAudio
        }

    val canContinue: Boolean
        get() = activeSelectedAudio != null && !isRecording && !isImportingAudio && !isUploadingPost

    val canSwitchSource: Boolean
        get() = !isRecording && !isImportingAudio && !isUploadingPost

}
