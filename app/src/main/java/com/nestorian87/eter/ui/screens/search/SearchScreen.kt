package com.nestorian87.eter.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.ui.components.PostPlayerViewModel
import com.nestorian87.eter.ui.components.TextAction
import com.nestorian87.eter.ui.screens.feed.PostListScreenContent
import com.nestorian87.eter.ui.screens.feed.PostListUiState
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onOpenPost: (Long) -> Unit = {},
    onOpenComments: (Long) -> Unit = {},
    onCategoryClick: ((Long) -> Unit)? = null,
    pendingCategoryId: Long? = null,
    onPendingCategoryConsumed: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PostPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pendingCategoryId) {
        if (pendingCategoryId != null) {
            viewModel.onCategorySelected(pendingCategoryId)
            onPendingCategoryConsumed()
        }
    }

    SearchScreenContent(
        uiState = uiState,
        activePlaybackPostId = playerUiState.activePost?.postId,
        isActivePostPlaying = playerUiState.isPlaying,
        modifier = modifier,
        onRetry = viewModel::loadInitial,
        onLoadMore = viewModel::onLoadMore,
        onToggleLike = viewModel::onToggleLike,
        onTogglePlayback = { post -> playerViewModel.togglePlayback(post) },
        onOpenPost = onOpenPost,
        onOpenComments = onOpenComments,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSortSelected = viewModel::onSortSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onResetSearchParameters = viewModel::onResetSearchParameters,
        onCategoryClick = onCategoryClick,
    )
}

@Composable
private fun SearchScreenContent(
    modifier: Modifier = Modifier,
    uiState: SearchUiState,
    activePlaybackPostId: Long?,
    isActivePostPlaying: Boolean,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onTogglePlayback: (Post) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenComments: (Long) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSortSelected: (SearchSortOption) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onResetSearchParameters: () -> Unit,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    val hasSearchCriteria = uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null
    PostListScreenContent(
        title = stringResource(R.string.search),
        listState = uiState.posts,
        activePlaybackPostId = activePlaybackPostId,
        isActivePostPlaying = isActivePostPlaying,
        emptyTitle = stringResource(R.string.feed_search_empty_title),
        emptySubtitle = stringResource(R.string.feed_search_empty_subtitle),
        modifier = modifier,
        hideEmptyState = !hasSearchCriteria,
        showEmptyRetryButton = false,
        controls = {
            SearchControls(
                uiState = uiState,
                onSearchQueryChanged = onSearchQueryChanged,
                onSortSelected = onSortSelected,
                onCategorySelected = onCategorySelected,
                onResetSearchParameters = onResetSearchParameters,
            )
        },
        onRetry = onRetry,
        onLoadMore = onLoadMore,
        onToggleLike = onToggleLike,
        onTogglePlayback = onTogglePlayback,
        onOpenPost = onOpenPost,
        onOpenComments = onOpenComments,
        onCategoryClick = onCategoryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchControls(
    uiState: SearchUiState,
    onSearchQueryChanged: (String) -> Unit,
    onSortSelected: (SearchSortOption) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onResetSearchParameters: () -> Unit,
) {
    var showSearchParameters by rememberSaveable { mutableStateOf(false) }
    val selectedCategory = uiState.categories.firstOrNull { it.categoryId == uiState.selectedCategoryId }

    Column(verticalArrangement = Arrangement.spacedBy(EterSpacing.medium)) {
        SearchField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = stringResource(R.string.feed_search_placeholder),
            trailingAction = {
                Spacer(modifier = Modifier.width(EterSpacing.small))
                SearchParametersTrigger(
                    selectedCategory = selectedCategory,
                    selectedSort = uiState.selectedSort,
                    onClick = { showSearchParameters = true },
                )
            },
        )
    }

    if (showSearchParameters) {
        SearchParametersSheet(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            selectedSort = uiState.selectedSort,
            isLoading = uiState.isLoadingCategories,
            onDismiss = {},
            onCategorySelected = { categoryId ->
                onCategorySelected(categoryId)
            },
            onSortSelected = onSortSelected,
            onResetSearchParameters = {
                onResetSearchParameters()
            },
        )
    }
}

@Composable
private fun SearchParametersTrigger(
    selectedCategory: PostCategory?,
    selectedSort: SearchSortOption,
    onClick: () -> Unit,
) {
    val isSelected = selectedCategory != null || selectedSort != SearchSortOption.POPULAR
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)
            },
        ),
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Outlined.FilterList,
            contentDescription = stringResource(R.string.search),
            modifier = Modifier
                .size(32.dp)
                .padding(8.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchParametersSheet(
    categories: List<PostCategory>,
    selectedCategoryId: Long?,
    selectedSort: SearchSortOption,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onSortSelected: (SearchSortOption) -> Unit,
    onResetSearchParameters: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredCategories = if (searchQuery.isBlank()) categories
    else categories.filter { it.categoryName.contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .imePadding()
                .padding(horizontal = EterSpacing.medium)
                .padding(bottom = EterSpacing.large),
            verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.search_parameters),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextAction(
                    text = stringResource(R.string.search_reset_parameters),
                    onClick = onResetSearchParameters,
                )
            }
            SearchSortRow(
                selectedSort = selectedSort,
                onSortSelected = onSortSelected,
            )
            Text(
                text = stringResource(R.string.feed_category_filter),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SearchCategorySearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
            )
            when {
                isLoading && categories.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.feed_categories_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
                else -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                        ),
                    ) {
                        LazyColumn {
                            item(key = "all_categories") {
                                SearchCategoryPickerRow(
                                    name = stringResource(R.string.feed_category_all),
                                    isSelected = selectedCategoryId == null,
                                    onClick = { onCategorySelected(null) },
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                                )
                            }
                            itemsIndexed(
                                items = filteredCategories,
                                key = { _, category -> category.categoryId },
                            ) { index, category ->
                                SearchCategoryPickerRow(
                                    name = category.categoryName,
                                    isSelected = category.categoryId == selectedCategoryId,
                                    onClick = { onCategorySelected(category.categoryId) },
                                )
                                if (index != filteredCategories.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCategoryPickerRow(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SearchCategorySearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.small,
            )
            .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(EterSpacing.small))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = stringResource(R.string.publish_categories_search_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun SearchSortRow(
    selectedSort: SearchSortOption,
    onSortSelected: (SearchSortOption) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
    ) {
        SearchSortOption.entries.forEach { option ->
            SearchSelectableChip(
                text = stringResource(option.labelRes),
                selected = selectedSort == option,
                onClick = { onSortSelected(option) },
            )
        }
    }
}

@Composable
private fun SearchSelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)
            },
        ),
        shape = MaterialTheme.shapes.small,
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = EterSpacing.medium, vertical = 10.dp),
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingAction: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = EterSpacing.xSmall),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.xSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(EterSpacing.small))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            trailingAction?.invoke()
        }
    }
}

@EterScreenPreviews
@Composable
private fun SearchScreenPreview() {
    EterPreview {
        SearchScreenContent(
            uiState = SearchUiState(
                posts = PostListUiState(isInitialLoading = false),
            ),
            activePlaybackPostId = null,
            isActivePostPlaying = false,
            onRetry = {},
            onLoadMore = {},
            onToggleLike = {},
            onTogglePlayback = {},
            onOpenPost = {},
            onOpenComments = {},
            onSearchQueryChanged = {},
            onSortSelected = {},
            onCategorySelected = {},
            onResetSearchParameters = {},
        )
    }
}
