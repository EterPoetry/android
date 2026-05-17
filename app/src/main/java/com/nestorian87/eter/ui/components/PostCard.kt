package com.nestorian87.eter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.theme.EterSpacing
import com.nestorian87.eter.ui.theme.PillShape

@Composable
fun PostCard(
    item: PostCardUiModel,
    isLikePending: Boolean,
    isPlaybackActive: Boolean,
    isPlaying: Boolean,
    onToggleLike: () -> Unit,
    onTogglePlayback: () -> Unit,
    onOpenPost: () -> Unit,
    onOpenComments: () -> Unit,
    modifier: Modifier = Modifier,
    onCategoryClick: ((Long) -> Unit)? = null,
) {
    val post = item.post

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenPost),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        ),
    ) {
        BoxWithConstraints {
            val isWideCard = maxWidth >= 760.dp

            if (isWideCard) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 152.dp)
                        .padding(EterSpacing.large),
                    horizontalArrangement = Arrangement.spacedBy(EterSpacing.xLarge),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                    ) {
                        Text(
                            text = post.title.orEmpty().ifBlank { stringResource(R.string.post_untitled) },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        PostCardAuthorLine(
                            originAuthorName = post.originAuthorName,
                            authorName = post.author?.name ?: stringResource(R.string.post_unknown_author),
                            authorUsername = post.author?.username,
                            authorPhotoUrl = post.author?.photo,
                        )

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

                        if (!post.description.isNullOrBlank()) {
                            Text(
                                text = post.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EterSpacing.large),
                    ) {
                        CompactStat(
                            icon = if (item.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            text = post.likesCount.toCompactCountText(),
                            contentDescription = stringResource(R.string.feed_like_post),
                            tint = if (item.isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textColor = if (item.isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            enabled = !isLikePending,
                            onClick = onToggleLike,
                        )
                        CompactStat(
                            icon = Icons.Rounded.ChatBubbleOutline,
                            text = post.commentsCount.toCompactCountText(),
                            contentDescription = stringResource(R.string.feed_open_comments),
                            onClick = onOpenComments,
                        )
                        CompactStat(
                            icon = Icons.Rounded.PlayArrow,
                            text = post.listens.toCompactCountText(),
                            contentDescription = null,
                        )
                        FeedMetaPill(text = post.audioDurationSeconds.toDurationText())
                        PlaybackSurface(
                            isPlaying = isPlaying,
                            isPlaybackActive = isPlaybackActive,
                            onClick = onTogglePlayback,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 156.dp)
                        .padding(EterSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = post.title.orEmpty().ifBlank { stringResource(R.string.post_untitled) },
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        PlaybackSurface(
                            isPlaying = isPlaying,
                            isPlaybackActive = isPlaybackActive,
                            onClick = onTogglePlayback,
                        )
                    }

                    PostCardAuthorLine(
                        originAuthorName = post.originAuthorName,
                        authorName = post.author?.name ?: stringResource(R.string.post_unknown_author),
                        authorUsername = post.author?.username,
                        authorPhotoUrl = post.author?.photo,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FeedMetaPill(text = post.audioDurationSeconds.toDurationText())
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

                    if (!post.description.isNullOrBlank()) {
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                    ) {
                        CompactStat(
                            icon = if (item.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            text = post.likesCount.toCompactCountText(),
                            contentDescription = stringResource(R.string.feed_like_post),
                            tint = if (item.isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textColor = if (item.isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            enabled = !isLikePending,
                            onClick = onToggleLike,
                        )
                        CompactStat(
                            icon = Icons.Rounded.ChatBubbleOutline,
                            text = post.commentsCount.toCompactCountText(),
                            contentDescription = stringResource(R.string.feed_open_comments),
                            onClick = onOpenComments,
                        )
                        CompactStat(
                            icon = Icons.Rounded.PlayArrow,
                            text = post.listens.toCompactCountText(),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCardAuthorLine(
    originAuthorName: String?,
    authorName: String,
    authorUsername: String?,
    authorPhotoUrl: String?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!originAuthorName.isNullOrBlank()) {
            Text(
                text = originAuthorName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AuthorInfoRow(
            name = authorName,
            username = authorUsername,
            photoUrl = authorPhotoUrl,
            avatarSize = 26.dp,
        )
    }
}

@Composable
private fun FeedMetaPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = PillShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlaybackSurface(
    isPlaying: Boolean,
    isPlaybackActive: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = when {
            isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            isPlaybackActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isPlaying -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                isPlaybackActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            },
        ),
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) {
                    stringResource(R.string.player_pause)
                } else {
                    stringResource(R.string.player_resume)
                },
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun CompactStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
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
            modifier = Modifier.size(20.dp),
            tint = tint,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

@Composable
fun PostCardSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 180.dp)
                .padding(EterSpacing.large),
            verticalArrangement = Arrangement.spacedBy(EterSpacing.medium),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(22.dp)
                    .shimmer(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(22.dp)
                    .shimmer(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.xSmall),
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .shimmer(),
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .shimmer(),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(13.dp)
                    .shimmer(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .shimmer(),
                    )
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(16.dp)
                            .shimmer(),
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .shimmer(),
                    )
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(16.dp)
                            .shimmer(),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(16.dp)
                        .shimmer(),
                )
                Spacer(modifier = Modifier.size(EterSpacing.small))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shimmer(),
                )
            }
        }
    }
}

@Composable
internal fun CategoryChip(name: String, onClick: (() -> Unit)? = null) {
    val content: @Composable () -> Unit = {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            text = "#${name.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    val chipColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val chipBorder = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
    if (onClick != null) {
        Surface(
            shape = PillShape,
            color = chipColor,
            border = chipBorder,
            onClick = onClick,
            content = content,
        )
    } else {
        Surface(
            shape = PillShape,
            color = chipColor,
            border = chipBorder,
            content = content,
        )
    }
}
