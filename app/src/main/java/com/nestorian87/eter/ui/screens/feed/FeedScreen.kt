package com.nestorian87.eter.ui.screens.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.ui.components.PostPlayerViewModel
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onOpenPost: (Long) -> Unit = {},
    onOpenComments: (Long) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onCategoryClick: ((Long) -> Unit)? = null,
    viewModel: FeedViewModel = hiltViewModel(),
    playerViewModel: PostPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    FeedScreenContent(
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
        onOpenSearch = onOpenSearch,
        onCategoryClick = onCategoryClick,
    )
}

@Composable
private fun FeedScreenContent(
    modifier: Modifier = Modifier,
    uiState: FeedUiState,
    activePlaybackPostId: Long?,
    isActivePostPlaying: Boolean,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onTogglePlayback: (Post) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    PostListScreenContent(
        title = stringResource(R.string.feed_popular_title),
        listState = uiState.posts,
        activePlaybackPostId = activePlaybackPostId,
        isActivePostPlaying = isActivePostPlaying,
        emptyTitle = stringResource(R.string.feed_empty_title),
        emptySubtitle = stringResource(R.string.feed_empty_subtitle),
        modifier = modifier,
        headerAction = {
            IconButton(onClick = onOpenSearch) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
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

@EterScreenPreviews
@Composable
private fun FeedScreenPreview() {
    EterPreview {
        FeedScreenContent(
            uiState = FeedUiState(
                posts = PostListUiState(
                    isInitialLoading = false,
                ),
            ),
            activePlaybackPostId = null,
            isActivePostPlaying = false,
            onRetry = {},
            onLoadMore = {},
            onToggleLike = { _ -> },
            onTogglePlayback = { _ -> },
            onOpenPost = { _ -> },
            onOpenComments = { _ -> },
            onOpenSearch = {},
        )
    }
}
