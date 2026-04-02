package com.nestorian87.eter.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EterLoadingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    spacing: Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val transition = rememberInfiniteTransition(label = "eter_loading")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(3) { index ->
            val animatedScale by transition.animateFloat(
                initialValue = 0.72f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 520,
                        delayMillis = index * 120,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "eter_loading_scale_$index",
            )
            val animatedAlpha by transition.animateFloat(
                initialValue = 0.45f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 520,
                        delayMillis = index * 120,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "eter_loading_alpha_$index",
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(animatedScale)
                    .alpha(animatedAlpha)
                    .background(
                        color = color,
                        shape = CircleShape,
                    ),
            )
        }
    }
}
