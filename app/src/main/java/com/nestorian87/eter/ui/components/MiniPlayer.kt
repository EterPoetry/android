package com.nestorian87.eter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier,
    onOpenPost: (Long) -> Unit = {},
    onOpenComments: (Long) -> Unit = {},
    viewModel: PostPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activePost = uiState.activePost ?: return
    MiniPlayerContent(
        post = activePost,
        isPlaying = uiState.isPlaying,
        progressPercent = uiState.progressPercent,
        currentTimeSeconds = uiState.currentTimeSeconds,
        durationSeconds = uiState.durationSeconds.takeIf { it > 0f } ?: (activePost.audioDurationSeconds ?: 0).toFloat(),
        onTogglePlayback = { viewModel.togglePlayback(activePost) },
        onSeek = viewModel::seekToPercent,
        onToggleLike = { viewModel.toggleLike(activePost) },
        onClose = viewModel::closePlayer,
        onOpenPost = { onOpenPost(activePost.postId) },
        onOpenComments = { onOpenComments(activePost.postId) },
        modifier = modifier,
    )
}

@Composable
private fun MiniPlayerContent(
    post: Post,
    isPlaying: Boolean,
    progressPercent: Float,
    currentTimeSeconds: Float,
    durationSeconds: Float,
    onTogglePlayback: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleLike: () -> Unit,
    onClose: () -> Unit,
    onOpenPost: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingSeekPercent by remember(post.postId, progressPercent) {
        mutableFloatStateOf(progressPercent.coerceIn(0f, 100f))
    }
    var isSeeking by remember(post.postId) { androidx.compose.runtime.mutableStateOf(false) }
    val authorName = post.author?.name ?: stringResource(R.string.post_unknown_author)
    val authorUsername = post.author?.username

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
                ),
        ) {
            // Main row: avatar | info | controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenPost)
                    .padding(
                        start = EterSpacing.large,
                        end = EterSpacing.small,
                        top = EterSpacing.medium,
                        bottom = EterSpacing.small,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
            ) {
                AuthorAvatar(
                    name = authorName,
                    photoUrl = post.author?.photo,
                    size = 40.dp,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = post.title.orEmpty().ifBlank { stringResource(R.string.post_untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (authorUsername != null) {
                        Text(
                            text = "@$authorUsername",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                // Action buttons — stop propagation via individual click handlers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    MiniIconButton(onClick = onToggleLike) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = if (post.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    MiniIconButton(onClick = onOpenComments) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    CircularPlayerButton(
                        playing = isPlaying,
                        size = 48.dp,
                        onClick = onTogglePlayback,
                    )
                    MiniIconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.player_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Progress row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = EterSpacing.large, end = EterSpacing.large, bottom = EterSpacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
            ) {
                Text(
                    text = currentTimeSeconds.toPlayerTimeText(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EterSlider(
                    modifier = Modifier.weight(1f),
                    value = if (isSeeking) pendingSeekPercent else progressPercent.coerceIn(0f, 100f),
                    onValueChange = { value ->
                        isSeeking = true
                        pendingSeekPercent = value
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        onSeek(pendingSeekPercent)
                    },
                    valueRange = 0f..100f,
                )
                Text(
                    text = durationSeconds.toPlayerTimeText(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MiniIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun CircularPlayerButton(
    playing: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (playing) {
                    stringResource(R.string.player_pause)
                } else {
                    stringResource(R.string.player_resume)
                },
                modifier = Modifier.size(size * 0.46f),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}


private fun Float.toPlayerTimeText(): String {
    val totalSeconds = toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Preview(showBackground = true, locale = "uk", widthDp = 412)
@Composable
private fun MiniPlayerPreview() {
    EterPreview {
        MiniPlayerContent(
            post = Post(
                postId = 1L,
                title = "Йоу",
                description = null,
                text = null,
                audioFileName = null,
                audioFileUrl = null,
                audioDurationSeconds = 6,
                status = com.nestorian87.eter.domain.model.PostStatus.PUBLISHED,
                listens = 4,
                likesCount = 4,
                isLiked = false,
                commentsCount = 4,
                originAuthorName = null,
                textSynchronization = emptyList(),
                categories = emptyList(),
                authorId = 2L,
                author = com.nestorian87.eter.domain.model.PostAuthor(
                    userId = 2L,
                    name = "Єгор Белік",
                    username = "yehor",
                    photo = null,
                    isPremium = false,
                ),
                createdAt = "",
                updatedAt = "",
            ),
            isPlaying = false,
            progressPercent = 22f,
            currentTimeSeconds = 0f,
            durationSeconds = 6f,
            onTogglePlayback = {},
            onSeek = {},
            onToggleLike = {},
            onClose = {},
            onOpenPost = {},
            onOpenComments = {},
        )
    }
}
