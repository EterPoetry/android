package com.nestorian87.eter.ui.screens.editpost

import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.ServerValidationException

sealed interface EditPostUiMessage {
    data class ResourceMessage(
        val resId: Int,
    ) : EditPostUiMessage

    enum class Validation : EditPostUiMessage {
        TITLE_REQUIRED,
        TEXT_REQUIRED,
        CATEGORY_LIMIT_REACHED,
        COPYRIGHT_CONFIRMATION_REQUIRED,
        AUDIO_REPLACE_FAILED,
    }

    data class ReasonMessage(
        val reason: PostException.Reason,
    ) : EditPostUiMessage

    data object Unexpected : EditPostUiMessage
}

fun Throwable.toEditPostUiMessage(): EditPostUiMessage = when (this) {
    is PostException -> EditPostUiMessage.ReasonMessage(primaryReason)
    is ServerValidationException -> EditPostUiMessage.ResourceMessage(
        resId = R.string.publish_invalid_data_error,
    )
    else -> EditPostUiMessage.Unexpected
}
