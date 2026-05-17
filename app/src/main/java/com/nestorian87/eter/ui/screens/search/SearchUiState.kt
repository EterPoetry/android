package com.nestorian87.eter.ui.screens.search

import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.ui.screens.feed.PostListUiState

data class SearchUiState(
    val posts: PostListUiState = PostListUiState(),
    val searchQuery: String = "",
    val selectedSort: SearchSortOption = SearchSortOption.POPULAR,
    val selectedCategoryId: Long? = null,
    val categories: List<PostCategory> = emptyList(),
    val isLoadingCategories: Boolean = false,
)
