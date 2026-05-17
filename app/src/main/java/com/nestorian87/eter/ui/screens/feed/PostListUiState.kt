package com.nestorian87.eter.ui.screens.feed

import com.nestorian87.eter.ui.components.PostCardUiModel

data class PostListUiState(
    val items: List<PostCardUiModel> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val error: Throwable? = null,
    val pendingLikePostIds: Set<Long> = emptySet(),
)
