package com.nestorian87.eter.ui.screens.feed

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.ui.components.EterLoadingIndicator
import com.nestorian87.eter.ui.components.PostCard
import com.nestorian87.eter.ui.components.PostCardSkeleton
import com.nestorian87.eter.ui.theme.EterSpacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun PostListScreenContent(
    title: String,
    listState: PostListUiState,
    activePlaybackPostId: Long?,
    isActivePostPlaying: Boolean,
    emptyTitle: String,
    emptySubtitle: String,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null,
    controls: (@Composable () -> Unit)? = null,
    hideEmptyState: Boolean = false,
    emptyStateMinimal: Boolean = false,
    showEmptyRetryButton: Boolean = true,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleLike: (Long) -> Unit,
    onTogglePlayback: (Post) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenComments: (Long) -> Unit,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(
        lazyListState,
        listState.items.size,
        listState.isInitialLoading,
        listState.isLoadingMore,
        listState.canLoadMore,
    ) {
        if (listState.isInitialLoading) {
            return@LaunchedEffect
        }

        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.filterNotNull()
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val triggerIndex = listState.items.lastIndex - 2
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
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                contentPadding = PaddingValues(
                    start = EterSpacing.xxLarge,
                    end = EterSpacing.xxLarge,
                    top = EterSpacing.section,
                    bottom = EterSpacing.hero,
                ),
            ) {
                item(key = "feed_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        headerAction?.invoke()
                    }
                }

                if (controls != null) {
                    item(key = "feed_controls") {
                        controls()
                    }
                }

                if (listState.isInitialLoading) {
                    items(count = 4, key = { index -> "skeleton_$index" }) {
                        PostCardSkeleton()
                    }
                } else if (listState.items.isEmpty()) {
                    if (!hideEmptyState) {
                        item(key = "feed_empty") {
                            PostListEmptyState(
                                title = emptyTitle,
                                subtitle = emptySubtitle,
                                onRetry = onRetry,
                                minimal = emptyStateMinimal,
                                showRetryButton = showEmptyRetryButton,
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = listState.items,
                        key = { _, item -> item.post.postId },
                    ) { _, item ->
                        PostCard(
                            item = item,
                            isLikePending = listState.pendingLikePostIds.contains(item.post.postId),
                            isPlaybackActive = activePlaybackPostId == item.post.postId,
                            isPlaying = activePlaybackPostId == item.post.postId && isActivePostPlaying,
                            onToggleLike = { onToggleLike(item.post.postId) },
                            onTogglePlayback = { onTogglePlayback(item.post) },
                            onOpenPost = { onOpenPost(item.post.postId) },
                            onOpenComments = { onOpenComments(item.post.postId) },
                            onCategoryClick = onCategoryClick,
                        )
                    }

                    if (listState.isLoadingMore) {
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

                    if (listState.error != null) {
                        item(key = "error_footer") {
                            PostListErrorFooter(
                                messageResId = listState.error.toFeedMessageResId(),
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
private fun PostListEmptyState(
    title: String,
    subtitle: String,
    onRetry: () -> Unit,
    minimal: Boolean = false,
    showRetryButton: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!minimal) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (showRetryButton) {
                    Button(onClick = onRetry) {
                        Text(text = stringResource(R.string.feed_retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun PostListErrorFooter(
    @StringRes messageResId: Int,
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

internal fun Throwable.toFeedMessageResId(): Int = when ((this as? PostException)?.primaryReason) {
    PostException.Reason.NETWORK -> R.string.feed_error_network
    PostException.Reason.FORBIDDEN -> R.string.feed_error_forbidden
    else -> R.string.feed_error_generic
}
