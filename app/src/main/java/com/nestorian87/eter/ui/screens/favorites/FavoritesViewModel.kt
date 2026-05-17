package com.nestorian87.eter.ui.screens.favorites

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
class FavoritesViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    private val postLikeController: PostLikeController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var sourcePosts: List<Post> = emptyList()
    private var offset = 0

    init {
        observePostInteractions()
        loadInitial()
    }

    fun loadInitial() {
        loadJob?.cancel()
        sourcePosts = emptyList()
        offset = 0
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
                val visiblePosts = sourcePosts.filter { post ->
                    val override = overrides[post.postId]
                    override == null || override.isLikePending || override.isLiked != false
                }
                _uiState.update { current ->
                    current.copy(
                        posts = current.posts.copy(
                            items = mapUiItems(visiblePosts),
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
                postRepository.getLikedPosts(
                    offset = if (reset) 0 else offset,
                    limit = PAGE_SIZE,
                )
            }.onSuccess { page ->
                val mergedItems = if (reset) page.items else sourcePosts + page.items
                sourcePosts = mergedItems.distinctBy(Post::postId)
                offset = page.offset + page.items.size

                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.copy(
                            items = mapUiItems(sourcePosts),
                            isInitialLoading = false,
                            isLoadingMore = false,
                            canLoadMore = sourcePosts.size < page.total,
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
