package com.nestorian87.eter.ui.screens.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.ui.components.EterLoadingIndicator
import com.nestorian87.eter.ui.components.PostCard
import com.nestorian87.eter.ui.components.PostPlayerViewModel
import com.nestorian87.eter.ui.components.PostCardSkeleton
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onOpenPost: (Long) -> Unit = {},
    onOpenComments: (Long) -> Unit = {},
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
    )
}

@Composable
private fun FeedScreenContent(
    uiState: FeedUiState,
    activePlaybackPostId: Long?,
    isActivePostPlaying: Boolean,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onTogglePlayback: (com.nestorian87.eter.domain.model.Post) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenComments: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(
        listState,
        uiState.items.size,
        uiState.isInitialLoading,
        uiState.isLoadingMore,
        uiState.canLoadMore,
    ) {
        if (uiState.isInitialLoading) {
            return@LaunchedEffect
        }

        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.filterNotNull()
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val triggerIndex = uiState.items.lastIndex - 2
                if (lastVisibleIndex >= triggerIndex) {
                    onLoadMore()
                }
            }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = EterSpacing.xxLarge,
                    end = EterSpacing.xxLarge,
                    top = EterSpacing.section,
                    bottom = EterSpacing.hero,
                ),
            ) {
                item(key = "feed_header") {
                    Text(
                        text = stringResource(R.string.feed_popular_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                if (uiState.isInitialLoading) {
                    items(count = 4, key = { index -> "skeleton_$index" }) {
                        PostCardSkeleton()
                    }
                } else if (uiState.items.isEmpty()) {
                    item(key = "feed_empty") {
                        FeedEmptyState(onRetry = onRetry)
                    }
                } else {
                    itemsIndexed(
                        items = uiState.items,
                        key = { _, item -> item.post.postId },
                    ) { _, item ->
                        PostCard(
                            item = item,
                            isLikePending = uiState.pendingLikePostIds.contains(item.post.postId),
                            isPlaybackActive = activePlaybackPostId == item.post.postId,
                            isPlaying = activePlaybackPostId == item.post.postId && isActivePostPlaying,
                            onToggleLike = { onToggleLike(item.post.postId) },
                            onTogglePlayback = { onTogglePlayback(item.post) },
                            onOpenPost = { onOpenPost(item.post.postId) },
                            onOpenComments = { onOpenComments(item.post.postId) },
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item(key = "loading_more") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = EterSpacing.small),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                EterLoadingIndicator()
                            }
                        }
                    }

                    if (uiState.error != null) {
                        item(key = "error_footer") {
                            FeedErrorFooter(
                                messageResId = uiState.error.toFeedMessageResId(),
                                onRetry = onRetry,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}


@Composable
private fun FeedEmptyState(onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.feed_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.feed_empty_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.feed_retry))
            }
        }
    }
}

@Composable
private fun FeedErrorFooter(
    messageResId: Int,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.feed_retry))
            }
        }
    }
}

private fun Throwable.toFeedMessageResId(): Int = when ((this as? PostException)?.primaryReason) {
    PostException.Reason.NETWORK -> R.string.feed_error_network
    PostException.Reason.FORBIDDEN -> R.string.feed_error_forbidden
    else -> R.string.feed_error_generic
}

@EterScreenPreviews
@Composable
private fun FeedScreenPreview() {
    EterPreview {
        FeedScreenContent(
            uiState = FeedUiState(
                isInitialLoading = false,
                items = emptyList(),
            ),
            activePlaybackPostId = null,
            isActivePostPlaying = false,
            onRetry = {},
            onLoadMore = {},
            onToggleLike = {},
            onTogglePlayback = {},
            onOpenPost = {},
            onOpenComments = {},
        )
    }
}
