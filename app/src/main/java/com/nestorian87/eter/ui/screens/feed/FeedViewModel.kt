package com.nestorian87.eter.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.data.repository.PostInteractionStore
import com.nestorian87.eter.data.repository.PostLikeController
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.repository.PostRepository
import com.nestorian87.eter.ui.components.PostCardUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    private val postLikeController: PostLikeController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var didResetOnSnapshotExpired = false
    private var sourcePosts: List<Post> = emptyList()

    init {
        observePostInteractions()
        loadInitial()
    }

    private fun observePostInteractions() {
        viewModelScope.launch {
            postInteractionStore.overrides.collect { overrides ->
                _uiState.update { current ->
                    current.copy(
                        items = sourcePosts.map { post ->
                            val merged = postInteractionStore.apply(post)
                            PostCardUiModel(
                                post = merged,
                                isLiked = merged.isLiked,
                            )
                        },
                        pendingLikePostIds = overrides
                            .filterValues { it.isLikePending }
                            .keys,
                    )
                }
            }
        }
    }

    fun loadInitial() {
        if (loadJob?.isActive == true) {
            return
        }

        sourcePosts = emptyList()
        _uiState.update {
            it.copy(
                items = emptyList(),
                isInitialLoading = true,
                isLoadingMore = false,
                canLoadMore = true,
                error = null,
                snapshotId = null,
                nextCursor = null,
            )
        }
        loadPopularPage(reset = true)
    }

    fun onLoadMore() {
        val state = _uiState.value
        if (state.isInitialLoading || state.isLoadingMore || !state.canLoadMore) {
            return
        }

        _uiState.update {
            it.copy(
                isLoadingMore = true,
                error = null,
            )
        }
        loadPopularPage(reset = false)
    }

    fun onToggleLike(postId: Long) {
        val post = _uiState.value.items.firstOrNull { it.post.postId == postId }?.post ?: return
        postLikeController.toggleLike(post)
    }

    private fun loadPopularPage(reset: Boolean) {
        if (loadJob?.isActive == true) {
            return
        }

        loadJob = viewModelScope.launch {
            val currentState = _uiState.value
            val snapshotId = if (reset) null else currentState.snapshotId
            val cursor = if (reset) null else currentState.nextCursor

            runCatching {
                postRepository.getPopularPosts(
                    snapshotId = snapshotId,
                    cursor = cursor,
                    limit = PAGE_SIZE,
                )
            }.onSuccess { page ->
                didResetOnSnapshotExpired = false
                _uiState.update { state ->
                    val mergedItems = if (reset) {
                        page.items
                    } else {
                        sourcePosts + page.items
                    }
                    sourcePosts = mergedItems.distinctBy { item -> item.postId }
                    val uiItems = sourcePosts.map { item ->
                        val merged = postInteractionStore.apply(item)
                        PostCardUiModel(
                            post = merged,
                            isLiked = merged.isLiked,
                        )
                    }

                    state.copy(
                        items = uiItems,
                        isInitialLoading = false,
                        isLoadingMore = false,
                        canLoadMore = page.hasMore,
                        error = null,
                        snapshotId = page.snapshotId,
                        nextCursor = page.nextCursor,
                    )
                }
            }.onFailure { error ->
                if (
                    error is PostException &&
                    error.primaryReason == PostException.Reason.POPULAR_SNAPSHOT_EXPIRED &&
                    !didResetOnSnapshotExpired
                ) {
                    didResetOnSnapshotExpired = true
                    loadInitial()
                    return@launch
                }

                _uiState.update { state ->
                    state.copy(
                        isInitialLoading = false,
                        isLoadingMore = false,
                        error = error,
                    )
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 12
    }
}
