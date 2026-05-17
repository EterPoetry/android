package com.nestorian87.eter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.nestorian87.eter.ui.theme.EterSpacing
import kotlinx.coroutines.delay


@Composable
fun AudioPlaybackCard(
    mediaUri: String,
    title: String,
    playContentDescription: String,
    pauseContentDescription: String,
    modifier: Modifier = Modifier,
    subtitle: (@Composable (Long) -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    durationFallbackMs: Long = 0L,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    playButtonContainerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
    playButtonTint: Color = MaterialTheme.colorScheme.primary,
) {
    val context = LocalContext.current
    val resolvedMediaUri = mediaUri.resolveRemoteAssetUrl() ?: mediaUri
    val player = remember(resolvedMediaUri) {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            setMediaItem(MediaItem.fromUri(resolvedMediaUri))
            prepare()
        }
    }
    var isPlaying by remember(player) { mutableStateOf(false) }
    var durationMs by remember(player) { mutableLongStateOf(durationFallbackMs) }
    var positionMs by remember(player) { mutableLongStateOf(0L) }
    var seekPreviewProgress by remember(player) { mutableFloatStateOf(Float.NaN) }

    DisposableEffect(player, durationFallbackMs) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = player.duration.takeIf { it > 0L } ?: durationFallbackMs
                positionMs = player.currentPosition.coerceAtLeast(0L)

                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    positionMs = durationMs
                }
            }
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, isPlaying, durationFallbackMs) {
        durationMs = player.duration.takeIf { it > 0L } ?: durationFallbackMs
        positionMs = player.currentPosition.coerceAtLeast(0L)

        while (isPlaying) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            durationMs = player.duration.takeIf { it > 0L } ?: durationFallbackMs
            delay(250L)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        border = border,
        shadowElevation = shadowElevation,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(playButtonContainerColor)
                        .clickable {
                            if (player.playbackState == Player.STATE_ENDED) {
                                player.seekTo(0)
                            }

                            if (player.isPlaying) {
                                player.pause()
                            } else {
                                player.play()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Rounded.Pause
                        } else {
                            Icons.Rounded.PlayArrow
                        },
                        contentDescription = if (isPlaying) {
                            pauseContentDescription
                        } else {
                            playContentDescription
                        },
                        tint = playButtonTint,
                    )
                }
                Spacer(modifier = Modifier.width(EterSpacing.medium))
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        subtitle(durationMs)
                    }
                }
                if (trailingContent != null) {
                    trailingContent()
                }
            }
            Spacer(modifier = Modifier.height(EterSpacing.medium))
            EterSlider(
                modifier = Modifier.fillMaxWidth(),
                value = if (!seekPreviewProgress.isNaN()) {
                    seekPreviewProgress
                } else if (durationMs > 0L) {
                    (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                },
                onValueChange = { progress ->
                    val targetPositionMs = (durationMs * progress).toLong().coerceIn(0L, durationMs)
                    seekPreviewProgress = progress
                    positionMs = targetPositionMs
                    player.seekTo(targetPositionMs)
                },
                onValueChangeFinished = { seekPreviewProgress = Float.NaN },
                enabled = durationMs > 0L && player.isCurrentMediaItemSeekable,
            )
            Spacer(modifier = Modifier.height(EterSpacing.xSmall))
            Row {
                Text(
                    text = formatAudioDuration(positionMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatAudioDuration(durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


private fun formatAudioDuration(durationMs: Long): String {
    val totalSeconds = (durationMs.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
