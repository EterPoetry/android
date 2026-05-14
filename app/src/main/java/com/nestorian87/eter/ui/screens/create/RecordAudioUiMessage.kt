package com.nestorian87.eter.ui.screens.create

import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.ServerValidationException

sealed interface RecordAudioUiMessage {
    data class ResourceMessage(
        val resId: Int,
    ) : RecordAudioUiMessage

    enum class Validation : RecordAudioUiMessage {
        RECORD_PERMISSION_REQUIRED,
        SELECT_AUDIO_FIRST,
        AUDIO_IMPORT_FAILED,
        RECORDING_START_FAILED,
        RECORDING_STOP_FAILED,
    }

    data class ReasonMessage(
        val reason: PostException.Reason,
    ) : RecordAudioUiMessage

    data object Unexpected : RecordAudioUiMessage
}

fun Throwable.toRecordAudioUiMessage(): RecordAudioUiMessage = when (this) {
    is PostException -> RecordAudioUiMessage.ReasonMessage(primaryReason)
    is ServerValidationException -> RecordAudioUiMessage.ResourceMessage(
        resId = R.string.create_audio_invalid_data_error,
    )
    else -> RecordAudioUiMessage.Unexpected
}
