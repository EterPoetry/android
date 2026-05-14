package com.nestorian87.eter.ui.screens.create

import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostException

fun RecordAudioUiMessage.toMessageResId(): Int = when (this) {
    RecordAudioUiMessage.Unexpected -> R.string.create_audio_unexpected_error
    is RecordAudioUiMessage.ResourceMessage -> resId
    is RecordAudioUiMessage.Validation -> when (this) {
        RecordAudioUiMessage.Validation.RECORD_PERMISSION_REQUIRED ->
            R.string.create_audio_record_permission_required
        RecordAudioUiMessage.Validation.SELECT_AUDIO_FIRST ->
            R.string.create_audio_select_audio_first
        RecordAudioUiMessage.Validation.AUDIO_IMPORT_FAILED ->
            R.string.create_audio_import_failed
        RecordAudioUiMessage.Validation.RECORDING_START_FAILED ->
            R.string.create_audio_recording_start_failed
        RecordAudioUiMessage.Validation.RECORDING_STOP_FAILED ->
            R.string.create_audio_recording_stop_failed
    }

    is RecordAudioUiMessage.ReasonMessage -> reason.toMessageResId()
}

fun PostException.Reason.toMessageResId(): Int = when (this) {
    PostException.Reason.NETWORK -> R.string.create_audio_network_error
    PostException.Reason.AUDIO_DURATION_LIMIT_EXCEEDED ->
        R.string.create_audio_duration_limit_error
    PostException.Reason.POST_IS_STILL_PROCESSING ->
        R.string.create_audio_processing_error
    PostException.Reason.AUDIO_REPLACEMENT_NOT_ALLOWED ->
        R.string.create_audio_replace_not_allowed_error
    PostException.Reason.NOT_FOUND -> R.string.create_audio_post_not_found_error
    PostException.Reason.FORBIDDEN -> R.string.create_audio_forbidden_error
    PostException.Reason.UNKNOWN -> R.string.create_audio_unexpected_error
}
