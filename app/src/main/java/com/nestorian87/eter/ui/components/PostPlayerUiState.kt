package com.nestorian87.eter.ui.components

import com.nestorian87.eter.domain.model.Post

data class PostPlayerUiState(
    val activePost: Post? = null,
    val isPlaying: Boolean = false,
    val currentTimeSeconds: Float = 0f,
    val durationSeconds: Float = 0f,
    val volume: Float = 1f,
    val isMuted: Boolean = false,
    val isStartingPlayback: Boolean = false,
    val isSyncingListenProgress: Boolean = false,
    val isFinalizingListen: Boolean = false,
    val currentSessionId: String? = null,
    val lastReportedPositionMs: Long = 0L,
    val countedListenVersion: Long = 0L,
    val lastCountedPostId: Long? = null,
) {
    val progressPercent: Float
        get() = if (durationSeconds <= 0f) 0f else (currentTimeSeconds / durationSeconds) * 100f

}
