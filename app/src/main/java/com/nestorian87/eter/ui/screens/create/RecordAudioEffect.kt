package com.nestorian87.eter.ui.screens.create

sealed interface RecordAudioEffect {
    data class NavigateToEditPost(
        val postId: Long,
    ) : RecordAudioEffect
}
