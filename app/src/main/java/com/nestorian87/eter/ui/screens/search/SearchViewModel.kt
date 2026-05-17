package com.nestorian87.eter.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.data.repository.PostInteractionStore
import com.nestorian87.eter.data.repository.PostLikeController
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostSearchQuery
import com.nestorian87.eter.domain.repository.PostRepository
import com.nestorian87.eter.ui.components.PostCardUiModel
import com.nestorian87.eter.ui.screens.feed.PostListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    private val postLikeController: PostLikeController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SearchUiState(
            posts = idlePostListState(),
        ),
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var categoriesJob: Job? = null
    private var sourcePosts: List<Post> = emptyList()
    private var searchOffset = 0

    init {
        observePostInteractions()
        loadCategories()
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQuery = value.take(MAX_SEARCH_LENGTH)) }
        scheduleSearchReload()
    }

    fun onSortSelected(sort: SearchSortOption) {
        if (_uiState.value.selectedSort == sort) return
        _uiState.update { it.copy(selectedSort = sort) }
        loadInitial()
    }

    fun onCategorySelected(categoryId: Long?) {
        if (_uiState.value.selectedCategoryId == categoryId) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadInitial()
    }

    fun loadInitial() {
        loadJob?.cancel()
        if (!hasActiveCriteria(_uiState.value)) {
            sourcePosts = emptyList()
            searchOffset = 0
            _uiState.update {
                it.copy(posts = idlePostListState())
            }
            return
        }
        sourcePosts = emptyList()
        searchOffset = 0
        _uiState.update {
            it.copy(posts = PostListUiState(isInitialLoading = true))
        }
        loadPage(reset = true)
    }

    fun onLoadMore() {
        if (!hasActiveCriteria(_uiState.value)) return
        val state = _uiState.value.posts
        if (state.isInitialLoading || state.isLoadingMore || !state.canLoadMore) return
        _uiState.update {
            it.copy(posts = it.posts.copy(isLoadingMore = true, error = null))
        }
        loadPage(reset = false)
    }

    fun onToggleLike(postId: Long) {
        val post = _uiState.value.posts.items.firstOrNull { it.post.postId == postId }?.post ?: return
        postLikeController.toggleLike(post)
    }

    fun onResetSearchParameters() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedSort = SearchSortOption.POPULAR,
                selectedCategoryId = null,
            )
        }
        loadInitial()
    }

    private fun observePostInteractions() {
        viewModelScope.launch {
            postInteractionStore.overrides.collect { overrides ->
                _uiState.update { current ->
                    current.copy(
                        posts = current.posts.copy(
                            items = mapUiItems(sourcePosts),
                            pendingLikePostIds = overrides
                                .filterValues { it.isLikePending }
                                .keys,
                        ),
                    )
                }
            }
        }
    }

    private fun scheduleSearchReload() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadInitial()
        }
    }

    private fun loadCategories() {
        if (categoriesJob?.isActive == true) return
        categoriesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            runCatching {
                postRepository.getCategories()
            }.onSuccess { categories ->
                _uiState.update { it.copy(categories = categories, isLoadingCategories = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingCategories = false) }
            }
        }
    }

    private fun loadPage(reset: Boolean) {
        loadJob = viewModelScope.launch {
            val currentState = _uiState.value
            runCatching {
                postRepository.searchPosts(
                    query = PostSearchQuery(
                        search = currentState.searchQuery.trim().takeIf { it.isNotEmpty() },
                        categoryId = currentState.selectedCategoryId,
                        sortBy = currentState.selectedSort.apiValue,
                        offset = if (reset) 0 else searchOffset,
                        limit = PAGE_SIZE,
                    ),
                )
            }.onSuccess { page ->
                val mergedItems = if (reset) page.items else sourcePosts + page.items
                sourcePosts = mergedItems.distinctBy(Post::postId)
                searchOffset = page.offset + page.items.size
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
                if (error is CancellationException) throw error
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
        PostCardUiModel(post = merged, isLiked = merged.isLiked)
    }

    private fun hasActiveCriteria(state: SearchUiState): Boolean =
        state.searchQuery.isNotBlank() || state.selectedCategoryId != null

    private fun idlePostListState(): PostListUiState = PostListUiState(
        isInitialLoading = false,
        canLoadMore = false,
    )

    companion object {
        private const val MAX_SEARCH_LENGTH = 200
        private const val PAGE_SIZE = 12
        private const val SEARCH_DEBOUNCE_MS = 350L
    }
}
