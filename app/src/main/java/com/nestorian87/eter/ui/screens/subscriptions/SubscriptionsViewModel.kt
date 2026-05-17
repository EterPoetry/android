package com.nestorian87.eter.ui.screens.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.data.repository.PostInteractionStore
import com.nestorian87.eter.data.repository.PostLikeController
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.repository.PostRepository
import com.nestorian87.eter.ui.components.PostCardUiModel
import com.nestorian87.eter.ui.screens.feed.PostListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    private val postLikeController: PostLikeController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var sourcePosts: List<Post> = emptyList()
    private var nextCursor: String? = null

    init {
        observePostInteractions()
        loadInitial()
    }

    fun loadInitial() {
        loadJob?.cancel()
        sourcePosts = emptyList()
        nextCursor = null
        _uiState.update {
            it.copy(
                posts = PostListUiState(
                    isInitialLoading = true,
                ),
            )
        }
        loadPage(reset = true)
    }

    fun onLoadMore() {
        val state = _uiState.value.posts
        if (state.isInitialLoading || state.isLoadingMore || !state.canLoadMore) {
            return
        }

        _uiState.update {
            it.copy(
                posts = it.posts.copy(
                    isLoadingMore = true,
                    error = null,
                ),
            )
        }
        loadPage(reset = false)
    }

    fun onToggleLike(postId: Long) {
        val post = _uiState.value.posts.items.firstOrNull { it.post.postId == postId }?.post ?: return
        postLikeController.toggleLike(post)
    }

    private fun observePostInteractions() {
        viewModelScope.launch {
            postInteractionStore.overrides.collect { overrides ->
                _uiState.update { current ->
                    current.copy(
                        posts = current.posts.copy(
                            items = mapUiItems(sourcePosts),
                            pendingLikePostIds = overrides.filterValues { it.isLikePending }.keys,
                        ),
                    )
                }
            }
        }
    }

    private fun loadPage(reset: Boolean) {
        loadJob = viewModelScope.launch {
            runCatching {
                postRepository.getSubscriptionFeed(
                    cursor = if (reset) null else nextCursor,
                    limit = PAGE_SIZE,
                )
            }.onSuccess { page ->
                val mergedItems = if (reset) page.items else sourcePosts + page.items
                sourcePosts = mergedItems.distinctBy(Post::postId)
                nextCursor = page.nextCursor

                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.copy(
                            items = mapUiItems(sourcePosts),
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = page.hasMore,
                            error = null,
                        ),
                    )
                }
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }

                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.copy(
                            isInitialLoading = false,
                            isLoadingMore = false,
                            error = error,
                        ),
                    )
                }
            }
        }
    }

    private fun mapUiItems(posts: List<Post>): List<PostCardUiModel> = posts.map { post ->
        val merged = postInteractionStore.apply(post)
        PostCardUiModel(
            post = merged,
            isLiked = merged.isLiked,
        )
    }

    private companion object {
        const val PAGE_SIZE = 12
    }
}
