package com.nestorian87.eter.ui.components

import androidx.lifecycle.ViewModel
import com.nestorian87.eter.data.repository.PostLikeController
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.service.PostPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class PostPlayerViewModel @Inject constructor(
    private val postPlayerManager: PostPlayerManager,
    private val postLikeController: PostLikeController,
) : ViewModel() {
    val uiState: StateFlow<PostPlayerUiState> = postPlayerManager.uiState

    fun togglePlayback(post: Post) {
        postPlayerManager.togglePostPlayback(post)
    }

    fun pausePlayback() {
        postPlayerManager.pausePlayback()
    }

    fun seekToPercent(percent: Float) {
        postPlayerManager.seekToPercent(percent)
    }

    fun closePlayer() {
        postPlayerManager.closePlayer()
    }

    fun toggleLike(post: Post) {
        postLikeController.toggleLike(post)
    }
}
