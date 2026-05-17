package com.nestorian87.eter.ui.screens.favorites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.PostPlayerViewModel
import com.nestorian87.eter.ui.screens.feed.PostListScreenContent
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    onOpenPost: (Long) -> Unit = {},
    onOpenComments: (Long) -> Unit = {},
    onCategoryClick: ((Long) -> Unit)? = null,
    viewModel: FavoritesViewModel = hiltViewModel(),
    playerViewModel: PostPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()

    PostListScreenContent(
        title = androidx.compose.ui.res.stringResource(R.string.favorites_title),
        listState = uiState.posts,
        activePlaybackPostId = playerUiState.activePost?.postId,
        isActivePostPlaying = playerUiState.isPlaying,
        emptyTitle = androidx.compose.ui.res.stringResource(R.string.favorites_empty_title),
        emptySubtitle = androidx.compose.ui.res.stringResource(R.string.favorites_empty_subtitle),
        modifier = modifier,
        emptyStateMinimal = true,
        onRetry = viewModel::loadInitial,
        onLoadMore = viewModel::onLoadMore,
        onToggleLike = viewModel::onToggleLike,
        onTogglePlayback = { post -> playerViewModel.togglePlayback(post) },
        onOpenPost = onOpenPost,
        onOpenComments = onOpenComments,
        onCategoryClick = onCategoryClick,
    )
}

@EterScreenPreviews
@Composable
private fun FavoritesScreenPreview() {
    EterPreview {
        PostListScreenContent(
            title = androidx.compose.ui.res.stringResource(R.string.favorites_title),
            listState = com.nestorian87.eter.ui.screens.feed.PostListUiState(
                isInitialLoading = false,
            ),
            activePlaybackPostId = null,
            isActivePostPlaying = false,
            emptyTitle = androidx.compose.ui.res.stringResource(R.string.favorites_empty_title),
            emptySubtitle = androidx.compose.ui.res.stringResource(R.string.favorites_empty_subtitle),
            onRetry = {},
            onLoadMore = {},
            onToggleLike = { _ -> },
            onTogglePlayback = { _ -> },
            onOpenPost = { _ -> },
            onOpenComments = { _ -> },
        )
    }
}
