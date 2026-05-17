package com.nestorian87.eter.ui.screens.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCommentsSort
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.ServerValidationException
import com.nestorian87.eter.ui.components.AuthorInfoRow
import com.nestorian87.eter.ui.components.CategoryChip
import com.nestorian87.eter.ui.components.EterConfirmationDialog
import com.nestorian87.eter.ui.components.EterLoadingIndicator
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.SecondaryActionButton
import com.nestorian87.eter.ui.components.shimmer
import com.nestorian87.eter.ui.components.toCompactCountText
import com.nestorian87.eter.ui.components.toDurationText
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing
import com.nestorian87.eter.ui.theme.PillShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PostScreen(
    modifier: Modifier = Modifier,
    postId: Long,
    focusComments: Boolean = false,
    onBackClick: () -> Unit = {},
    onEditPost: (Long) -> Unit = {},
    onPostDeleted: () -> Unit = {},
    onCategoryClick: ((Long) -> Unit)? = null,
    viewModel: PostViewModel = hiltViewModel<PostViewModel, PostViewModel.Factory> { factory ->
        factory.create(postId)
    },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isPostDeleted) {
        if (uiState.isPostDeleted) {
            onPostDeleted()
        }
    }
    PostScreenContent(
        uiState = uiState,
        focusComments = focusComments,
        modifier = modifier,
        onBackClick = onBackClick,
        onEditPost = onEditPost,
        onDeletePost = viewModel::onDeletePost,
        onRetryLoadPost = viewModel::retryLoadPost,
        onTogglePostLike = viewModel::onTogglePostLike,
        onPlayPost = viewModel::onPlayPost,
        onSeekAndPlay = viewModel::onSeekAndPlay,
        onToggleCommentLike = viewModel::onToggleCommentLike,
        onDeleteComment = viewModel::onDeleteComment,
        onSortChanged = viewModel::onSortChanged,
        onLoadMoreComments = viewModel::loadMoreComments,
        onCommentComposerChanged = viewModel::onCommentComposerChanged,
        onSendComment = viewModel::onSendComment,
        onReplyComposerOpen = viewModel::onReplyComposerOpen,
        onReplyComposerDismiss = viewModel::onReplyComposerDismiss,
        onReplyDraftChanged = viewModel::onReplyDraftChanged,
        onSendReply = viewModel::onSendReply,
        onToggleReplies = viewModel::onToggleReplies,
        onLoadMoreReplies = viewModel::onLoadMoreReplies,
        onCategoryClick = onCategoryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostScreenContent(
    uiState: PostUiState,
    focusComments: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onEditPost: (Long) -> Unit,
    onDeletePost: () -> Unit,
    onRetryLoadPost: () -> Unit,
    onTogglePostLike: () -> Unit,
    onPlayPost: () -> Unit,
    onSeekAndPlay: (Int) -> Unit,
    onToggleCommentLike: (Long, Long?) -> Unit,
    onDeleteComment: (Long, Long?) -> Unit,
    onSortChanged: (PostCommentsSort) -> Unit,
    onLoadMoreComments: () -> Unit,
    onCommentComposerChanged: (String) -> Unit,
    onSendComment: () -> Unit,
    onReplyComposerOpen: (Long) -> Unit,
    onReplyComposerDismiss: () -> Unit,
    onReplyDraftChanged: (Long, String) -> Unit,
    onSendReply: (Long) -> Unit,
    onToggleReplies: (Long) -> Unit,
    onLoadMoreReplies: (Long) -> Unit,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    val mobileBreakpointPx = with(LocalDensity.current) { MOBILE_BREAKPOINT_DP.dp.roundToPx() }
    val isMobile = LocalWindowInfo.current.containerSize.width <= mobileBreakpointPx
    var isCommentsSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(isMobile) {
        if (!isMobile) {
            isCommentsSheetOpen = false
        }
    }

    LaunchedEffect(focusComments, isMobile) {
        if (focusComments && isMobile) {
            isCommentsSheetOpen = true
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
            when {
                uiState.isLoadingPost -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(horizontal = EterSpacing.xxLarge, vertical = EterSpacing.section),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.navigation_back),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            EterLoadingIndicator()
                        }
                    }
                }

                uiState.post == null -> {
                    PostLoadErrorState(
                        messageResId = uiState.postError.toPostMessageResId(),
                        onRetry = onRetryLoadPost,
                        onBackClick = onBackClick,
                    )
                }

                else -> {
                    val post = uiState.post
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = EterSpacing.xxLarge, vertical = EterSpacing.section),
                        verticalArrangement = Arrangement.spacedBy(EterSpacing.large),
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.navigation_back),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        PostHeaderCard(
                            post = post,
                            currentUserId = uiState.currentUserId,
                            isPostLiked = uiState.isPostLiked,
                            isPostLikePending = uiState.pendingPostLike,
                            isCurrentPostPlaying = uiState.isCurrentPostPlaying,
                            onTogglePostLike = onTogglePostLike,
                            onPlayPost = onPlayPost,
                            onOpenComments = { isCommentsSheetOpen = true },
                            onEditPost = { onEditPost(post.postId) },
                            onDeletePost = { isDeleteDialogVisible = true },
                            isMobile = isMobile,
                            onCategoryClick = onCategoryClick,
                        )

                        if (!post.text.isNullOrBlank()) {
                            PostBodyCard(
                                title = stringResource(R.string.post_text_title),
                                text = post.text,
                                synchronization = post.textSynchronization,
                                activePlaybackTimeMs = uiState.currentPlaybackTimeMs,
                                isPlaybackActive = uiState.isCurrentPostActive,
                                onLineClick = onSeekAndPlay,
                            )
                        }

                        if (!isMobile) {
                            CommentsPanel(
                                uiState = uiState,
                                requestComposerFocus = focusComments,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 520.dp, max = 760.dp),
                                onToggleCommentLike = onToggleCommentLike,
                                onDeleteComment = onDeleteComment,
                                onSortChanged = onSortChanged,
                                onLoadMoreComments = onLoadMoreComments,
                                onCommentComposerChanged = onCommentComposerChanged,
                                onSendComment = onSendComment,
                                onReplyComposerOpen = onReplyComposerOpen,
                                onReplyComposerDismiss = onReplyComposerDismiss,
                                onReplyDraftChanged = onReplyDraftChanged,
                                onSendReply = onSendReply,
                                onToggleReplies = onToggleReplies,
                                onLoadMoreReplies = onLoadMoreReplies,
                            )
                        }

                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }

    if (isMobile && isCommentsSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { },
            sheetState = commentsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            CommentsPanel(
                uiState = uiState,
                requestComposerFocus = focusComments,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .imePadding(),
                onToggleCommentLike = onToggleCommentLike,
                onDeleteComment = onDeleteComment,
                onSortChanged = onSortChanged,
                onLoadMoreComments = onLoadMoreComments,
                onCommentComposerChanged = onCommentComposerChanged,
                onSendComment = onSendComment,
                onReplyComposerOpen = onReplyComposerOpen,
                onReplyComposerDismiss = onReplyComposerDismiss,
                onReplyDraftChanged = onReplyDraftChanged,
                onSendReply = onSendReply,
                onToggleReplies = onToggleReplies,
                onLoadMoreReplies = onLoadMoreReplies,
            )
        }
    }

    if (isDeleteDialogVisible) {
        EterConfirmationDialog(
            title = stringResource(R.string.post_delete_title),
            message = stringResource(R.string.post_delete_message),
            confirmText = stringResource(R.string.post_delete_confirm),
            confirmIcon = Icons.Rounded.DeleteOutline,
            isConfirmLoading = uiState.isDeletingPost,
            errorMessage = uiState.postActionError?.let { stringResource(it.toPostMessageResId()) },
            onConfirm = onDeletePost,
            onDismiss = {},
        )
    }
}

@Composable
private fun PostHeaderCard(
    post: Post,
    currentUserId: Long?,
    isPostLiked: Boolean,
    isPostLikePending: Boolean,
    isCurrentPostPlaying: Boolean,
    onTogglePostLike: () -> Unit,
    onPlayPost: () -> Unit,
    onOpenComments: () -> Unit,
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    isMobile: Boolean,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    val displayAuthorName = post.author?.name ?: stringResource(R.string.post_unknown_author)
    val isOwnedByCurrentUser = currentUserId != null && currentUserId == post.authorId

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.large),
            verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
        ) {
            Text(
                text = post.title.orEmpty().ifBlank { stringResource(R.string.post_untitled) },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextMetaPill(text = post.audioDurationSeconds.toDurationText())
                MetaPill(
                    icon = Icons.Rounded.Schedule,
                    text = post.createdAt.toPostDateText(),
                )
                Spacer(modifier = Modifier.width(EterSpacing.xSmall))
                EngagementStat(
                    icon = if (isPostLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    text = post.likesCount.toCompactCountText(),
                    contentDescription = stringResource(R.string.feed_like_post),
                    tint = if (isPostLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textColor = if (isPostLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    enabled = !isPostLikePending,
                    onClick = onTogglePostLike,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
                EngagementStat(
                    icon = Icons.Rounded.PlayArrow,
                    text = post.listens.toCompactCountText(),
                    contentDescription = null,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
            }

            // Author info + play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                    ) {
                        if (!post.originAuthorName.isNullOrBlank()) {
                            Text(
                                text = post.originAuthorName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        AuthorInfoRow(
                            name = displayAuthorName,
                            username = post.author?.username,
                            photoUrl = post.author?.photo,
                            avatarSize = 32.dp,
                        )
                    }
                }
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)),
                    onClick = onPlayPost,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCurrentPostPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isCurrentPostPlaying) {
                                stringResource(R.string.post_pause_content_description)
                            } else {
                                stringResource(R.string.post_play_content_description)
                            },
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // Stats + like + comments row
            if (!isMobile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    CommentsEntryButton(
                        count = post.commentsCount,
                        onClick = onOpenComments,
                    )
                }
            }

            if (isMobile) {
                CommentsEntryButton(
                    count = post.commentsCount,
                    onClick = onOpenComments,
                )
            }

            if (post.categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EterSpacing.xSmall),
                ) {
                    post.categories.forEach { category ->
                        CategoryChip(
                            name = category.categoryName,
                            onClick = onCategoryClick?.let { { it(category.categoryId) } },
                        )
                    }
                }
            }

            if (isOwnedByCurrentUser) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                ) {
                    PrimaryActionButton(text = stringResource(R.string.post_edit), onClick = onEditPost)
                    SecondaryActionButton(
                        text = stringResource(R.string.post_delete),
                        outlineColor = MaterialTheme.colorScheme.error,
                        onClick = onDeletePost,
                    )
                }
            }

            if (!post.description.isNullOrBlank()) {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PostBodyCard(
    title: String,
    text: String,
    emphasisedTitle: Boolean = true,
    synchronization: List<com.nestorian87.eter.domain.model.PostTextSynchronization> = emptyList(),
    activePlaybackTimeMs: Long = 0L,
    isPlaybackActive: Boolean = false,
    onLineClick: (Int) -> Unit = {},
) {
    val lines = remember(text) { text.lines() }
    val synchronizationByLine = remember(synchronization) {
        synchronization.associateBy { it.lineIndex }
    }
    val activeLineIndex = remember(isPlaybackActive, activePlaybackTimeMs, synchronization) {
        if (!isPlaybackActive) {
            null
        } else {
            synchronization
                .filter { it.audioStartMomentMs.toLong() <= activePlaybackTimeMs }
                .maxByOrNull { it.audioStartMomentMs }
                ?.lineIndex
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.large),
            verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
        ) {
            Text(
                text = title,
                style = if (emphasisedTitle) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (text.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    lines.forEachIndexed { index, line ->
                        val sync = synchronizationByLine[index]
                        val isHighlighted = index == activeLineIndex
                        Text(
                            text = line.ifBlank { " " },
                            modifier = if (sync != null) {
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onLineClick(sync.audioStartMomentMs) }
                                    .background(
                                        if (isHighlighted) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            } else {
                                Modifier
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isHighlighted) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsEntryButton(
    count: Int,
    onClick: () -> Unit,
) {
    Surface(
        shape = PillShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.ChatBubbleOutline,
                contentDescription = stringResource(R.string.feed_open_comments),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.post_open_comments),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = count.toCompactCountText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CommentsPanel(
    uiState: PostUiState,
    requestComposerFocus: Boolean,
    onToggleCommentLike: (Long, Long?) -> Unit,
    onDeleteComment: (Long, Long?) -> Unit,
    onSortChanged: (PostCommentsSort) -> Unit,
    onLoadMoreComments: () -> Unit,
    onCommentComposerChanged: (String) -> Unit,
    onSendComment: () -> Unit,
    onReplyComposerOpen: (Long) -> Unit,
    onReplyComposerDismiss: () -> Unit,
    onReplyDraftChanged: (Long, String) -> Unit,
    onSendReply: (Long) -> Unit,
    onToggleReplies: (Long) -> Unit,
    onLoadMoreReplies: (Long) -> Unit,
    modifier: Modifier = Modifier,
    headerTrailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EterSpacing.large, vertical = EterSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.post_comments_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = (uiState.post?.commentsCount ?: uiState.comments.size).toCompactCountText(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (headerTrailing != null) {
                    Spacer(modifier = Modifier.width(EterSpacing.xSmall))
                    headerTrailing()
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
            ) {
                SortChip(
                    title = stringResource(R.string.post_comments_sort_newest),
                    selected = uiState.commentsSort == PostCommentsSort.NEWEST,
                    onClick = { onSortChanged(PostCommentsSort.NEWEST) },
                )
                SortChip(
                    title = stringResource(R.string.post_comments_sort_popular),
                    selected = uiState.commentsSort == PostCommentsSort.POPULAR,
                    onClick = { onSortChanged(PostCommentsSort.POPULAR) },
                )
            }

            if (uiState.commentsError != null) {
                Text(
                    modifier = Modifier.padding(horizontal = EterSpacing.medium),
                    text = stringResource(uiState.commentsError.toCommentsMessageResId()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
                contentPadding = PaddingValues(
                    horizontal = EterSpacing.large,
                    vertical = EterSpacing.medium,
                ),
            ) {
                items(
                    items = uiState.comments,
                    key = { item -> item.comment.commentId },
                ) { item ->
                    CommentItem(
                        item = item,
                        currentUserId = uiState.currentUserId,
                        parentCommentId = null,
                        pendingLikeCommentIds = uiState.pendingCommentLikeIds,
                        pendingDeleteCommentIds = uiState.pendingDeleteCommentIds,
                        replies = uiState.repliesByCommentId[item.comment.commentId].orEmpty(),
                        areRepliesExpanded = uiState.expandedRepliesCommentIds.contains(item.comment.commentId),
                        isLoadingReplies = uiState.loadingRepliesCommentIds.contains(item.comment.commentId),
                        isLoadingMoreReplies = uiState.loadingMoreRepliesCommentIds.contains(item.comment.commentId),
                        hasMoreReplies = uiState.repliesNextCursorByCommentId[item.comment.commentId] != null,
                        openReplyComposer = uiState.openReplyComposerCommentId == item.comment.commentId,
                        replyDraft = uiState.replyDraftByCommentId[item.comment.commentId].orEmpty(),
                        onToggleLike = { onToggleCommentLike(item.comment.commentId, null) },
                        onDelete = { onDeleteComment(item.comment.commentId, null) },
                        onReplyClick = { onReplyComposerOpen(item.comment.commentId) },
                        onDismissReply = onReplyComposerDismiss,
                        onReplyDraftChanged = { value ->
                            onReplyDraftChanged(item.comment.commentId, value)
                        },
                        onSendReply = { onSendReply(item.comment.commentId) },
                        onToggleReplies = { onToggleReplies(item.comment.commentId) },
                        onLoadMoreReplies = { onLoadMoreReplies(item.comment.commentId) },
                        onToggleReplyLike = onToggleCommentLike,
                        onDeleteReply = onDeleteComment,
                    )
                }

                if (uiState.isLoadingComments) {
                    items(count = 3, key = { "skeleton_$it" }) {
                        CommentSkeletonItem()
                    }
                }

                if (uiState.isLoadingMoreComments) {
                    item(key = "loading_more_comments") {
                        LoadingRow()
                    }
                } else if (uiState.commentsNextCursor != null) {
                    item(key = "load_more_comments") {
                        TextButton(onClick = onLoadMoreComments) {
                            Text(text = stringResource(R.string.post_comments_load_more))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
            CommentComposer(
                value = uiState.composerText,
                requestComposerFocus = requestComposerFocus,
                isSubmitting = uiState.isSubmittingComment,
                onValueChange = onCommentComposerChanged,
                onSend = onSendComment,
            )
        }
    }
}

@Composable
private fun CommentItem(
    item: PostCommentUiModel,
    currentUserId: Long?,
    parentCommentId: Long?,
    pendingLikeCommentIds: Set<Long>,
    pendingDeleteCommentIds: Set<Long>,
    replies: List<PostCommentUiModel>,
    areRepliesExpanded: Boolean,
    isLoadingReplies: Boolean,
    isLoadingMoreReplies: Boolean,
    hasMoreReplies: Boolean,
    openReplyComposer: Boolean,
    replyDraft: String,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit,
    onReplyClick: () -> Unit,
    onDismissReply: () -> Unit,
    onReplyDraftChanged: (String) -> Unit,
    onSendReply: () -> Unit,
    onToggleReplies: () -> Unit,
    onLoadMoreReplies: () -> Unit,
    onToggleReplyLike: (Long, Long?) -> Unit,
    onDeleteReply: (Long, Long?) -> Unit,
) {
    val isReply = parentCommentId != null
    val canReply = !isReply
    val isLikePending = pendingLikeCommentIds.contains(item.comment.commentId)
    val isDeletePending = pendingDeleteCommentIds.contains(item.comment.commentId)
    val isOwnedByCurrentUser = currentUserId != null && item.comment.authorId == currentUserId

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) EterSpacing.medium else 0.dp),
        verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    AuthorInfoRow(
                        name = item.comment.author?.name
                            ?: item.comment.author?.username
                            ?: stringResource(R.string.post_unknown_author),
                        username = item.comment.author?.username,
                        photoUrl = item.comment.author?.photo,
                        avatarSize = if (isReply) 28.dp else 32.dp,
                    )
                    item.comment.createdAt?.takeIf { it.isNotBlank() }?.let { createdAt ->
                        Text(
                            text = createdAt.toCommentDateTimeText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (item.comment.isLikedByAuthor) {
                        AuthorLikedBadge()
                    }
                }
                if (isOwnedByCurrentUser) {
                    IconButton(
                        onClick = onDelete,
                        enabled = !isDeletePending,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.post_comment_delete),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Text(
                text = item.comment.text.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EngagementStat(
                    icon = if (item.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    text = item.comment.likesCount.toCompactCountText(),
                    contentDescription = stringResource(R.string.post_comment_like),
                    tint = if (item.isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    enabled = !isLikePending,
                    onClick = onToggleLike,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
                if (canReply) {
                    ActionPill(
                        text = stringResource(R.string.post_comment_reply),
                        onClick = onReplyClick,
                    )
                }
            }

            if (openReplyComposer && canReply) {
                InlineReplyComposer(
                    value = replyDraft,
                    onValueChange = onReplyDraftChanged,
                    onSend = onSendReply,
                    onDismiss = onDismissReply,
                )
            }
        }

        if (!isReply && (item.comment.repliesCount > 0 || replies.isNotEmpty())) {
            ReplyToggle(
                expanded = areRepliesExpanded,
                count = item.comment.repliesCount.coerceAtLeast(replies.size),
                onClick = onToggleReplies,
            )
        }

        if (areRepliesExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(start = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 7.dp)
                        .width(1.5.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(2.dp),
                        ),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = EterSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    if (isLoadingReplies) {
                        LoadingRow()
                    }
                    replies.forEach { reply ->
                        CommentItem(
                            item = reply,
                            currentUserId = currentUserId,
                            parentCommentId = item.comment.commentId,
                            pendingLikeCommentIds = pendingLikeCommentIds,
                            pendingDeleteCommentIds = pendingDeleteCommentIds,
                            replies = emptyList(),
                            areRepliesExpanded = false,
                            isLoadingReplies = false,
                            isLoadingMoreReplies = false,
                            hasMoreReplies = false,
                            openReplyComposer = false,
                            replyDraft = "",
                            onToggleLike = {
                                onToggleReplyLike(reply.comment.commentId, item.comment.commentId)
                            },
                            onDelete = {
                                onDeleteReply(reply.comment.commentId, item.comment.commentId)
                            },
                            onReplyClick = {},
                            onDismissReply = {},
                            onReplyDraftChanged = {},
                            onSendReply = {},
                            onToggleReplies = {},
                            onLoadMoreReplies = {},
                            onToggleReplyLike = onToggleReplyLike,
                            onDeleteReply = onDeleteReply,
                        )
                    }
                    if (isLoadingMoreReplies) {
                        LoadingRow()
                    } else if (hasMoreReplies) {
                        TextButton(onClick = onLoadMoreReplies) {
                            Text(text = stringResource(R.string.post_comments_load_more_replies))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthorLikedBadge() {
    Surface(
        shape = PillShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = stringResource(R.string.post_comment_liked_by_author),
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.post_comment_liked_by_author),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ReplyToggle(
    expanded: Boolean,
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (expanded) {
                stringResource(R.string.post_comment_hide_replies)
            } else {
                stringResource(R.string.post_comment_show_replies, count)
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InlineReplyComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = stringResource(R.string.post_reply_placeholder))
            },
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            trailingIcon = {
                IconButton(onClick = onSend) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(R.string.post_comment_send),
                    )
                }
            },
        )
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(R.string.post_comment_cancel_reply))
        }
    }
}

@Composable
private fun SortChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = PillShape,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            },
        ),
        onClick = onClick,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = EterSpacing.medium, vertical = 10.dp),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommentComposer(
    value: String,
    requestComposerFocus: Boolean,
    isSubmitting: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(requestComposerFocus) {
        if (requestComposerFocus) {
            focusRequester.requestFocus()
            coroutineScope.launch {
                delay(220L)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.medium)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        coroutineScope.launch {
                            delay(220L)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
                .focusRequester(focusRequester),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = stringResource(R.string.post_comment_placeholder))
            },
            enabled = !isSubmitting,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            trailingIcon = {
                IconButton(
                    enabled = !isSubmitting,
                    onClick = onSend,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = stringResource(R.string.post_comment_send),
                    )
                }
            },
        )
    }
}

@Composable
private fun TextMetaPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
        shape = PillShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MetaPill(
    icon: ImageVector,
    text: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = PillShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


@Composable
private fun EngagementStat(
    icon: ImageVector,
    text: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            } else {
                Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
            }
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = tint,
        )
        Text(
            text = text,
            style = textStyle,
            color = textColor,
        )
    }
}

@Composable
private fun ActionPill(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = PillShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        onClick = onClick,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        EterLoadingIndicator()
    }
}

@Composable
private fun CommentSkeletonItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .shimmer(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(13.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer(),
            )
        }
    }
}

@Composable
private fun PostLoadErrorState(
    messageResId: Int,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(EterSpacing.section),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(messageResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(EterSpacing.medium))
        Row(horizontalArrangement = Arrangement.spacedBy(EterSpacing.small)) {
            TextButton(onClick = onBackClick) {
                Text(text = stringResource(R.string.post_back))
            }
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.post_retry))
            }
        }
    }
}

private fun Throwable?.toPostMessageResId(): Int = when ((this as? PostException)?.primaryReason) {
    PostException.Reason.NETWORK -> R.string.post_error_network
    PostException.Reason.NOT_FOUND -> R.string.post_error_not_found
    PostException.Reason.FORBIDDEN -> R.string.post_error_forbidden
    else -> R.string.post_error_generic
}

private fun Throwable.toCommentsMessageResId(): Int = when ((this as? PostException)?.primaryReason) {
    PostException.Reason.NETWORK -> R.string.post_comments_error_network
    PostException.Reason.FORBIDDEN -> R.string.post_comments_error_forbidden
    PostException.Reason.NOT_FOUND -> R.string.post_comments_error_not_found
    else -> when ((this as? ServerValidationException)?.reason) {
        ServerValidationException.Reason.INVALID_DATA -> R.string.post_comments_error_invalid_data
        ServerValidationException.Reason.CONFLICT -> R.string.post_comments_error_generic
        null -> R.string.post_comments_error_generic
    }
}

private fun String.toPostDateText(): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("uk"))
    return parseServerInstant(this)?.atZone(ZoneId.systemDefault())?.format(formatter) ?: this
}

private fun String.toCommentDateTimeText(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
    return parseServerInstant(this)?.atZone(ZoneId.systemDefault())?.format(formatter) ?: this
}

private fun parseServerInstant(value: String): Instant? = sequenceOf<() -> Instant?>(
    { runCatching { Instant.parse(value) }.getOrNull() },
    { runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull() },
    {
        runCatching {
            LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant()
        }.getOrNull()
    },
).firstNotNullOfOrNull { parser -> parser() }

private const val MOBILE_BREAKPOINT_DP = 640

@EterScreenPreviews
@Composable
private fun PostScreenPreview() {
    EterPreview {
        PostLoadErrorState(
            messageResId = R.string.post_error_generic,
            onRetry = {},
            onBackClick = {},
        )
    }
}
