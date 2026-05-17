package com.nestorian87.eter.ui.screens.editpost

import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostException

fun EditPostUiMessage.toMessageResId(): Int = when (this) {
    EditPostUiMessage.Unexpected -> R.string.publish_unexpected_error
    is EditPostUiMessage.ResourceMessage -> resId
    is EditPostUiMessage.Validation -> when (this) {
        EditPostUiMessage.Validation.TITLE_REQUIRED -> R.string.publish_title_required_error
        EditPostUiMessage.Validation.TEXT_REQUIRED -> R.string.publish_text_required_error
        EditPostUiMessage.Validation.CATEGORY_LIMIT_REACHED -> R.string.publish_category_limit_error
        EditPostUiMessage.Validation.COPYRIGHT_CONFIRMATION_REQUIRED ->
            R.string.publish_copyright_confirmation_required
        EditPostUiMessage.Validation.AUDIO_REPLACE_FAILED -> R.string.publish_replace_audio_failed
    }

    is EditPostUiMessage.ReasonMessage -> reason.toEditPostMessageResId()
}

fun PostException.Reason.toEditPostMessageResId(): Int = when (this) {
    PostException.Reason.NETWORK -> R.string.publish_network_error
    PostException.Reason.AUDIO_DURATION_LIMIT_EXCEEDED -> R.string.publish_duration_limit_error
    PostException.Reason.POST_IS_STILL_PROCESSING -> R.string.publish_processing_error
    PostException.Reason.AUDIO_REPLACEMENT_NOT_ALLOWED -> R.string.publish_audio_replace_not_allowed
    PostException.Reason.POPULAR_SNAPSHOT_EXPIRED -> R.string.publish_unexpected_error
    PostException.Reason.NOT_FOUND -> R.string.publish_post_not_found
    PostException.Reason.FORBIDDEN -> R.string.publish_forbidden_error
    PostException.Reason.UNKNOWN -> R.string.publish_unexpected_error
}
