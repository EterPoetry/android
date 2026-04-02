package com.nestorian87.eter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val LOADER_VISIBILITY_DELAY_MS = 180
private const val LOADER_FADE_DURATION_MS = 180

@Composable
fun PrimaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    val textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
    var isLoaderVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(LOADER_VISIBILITY_DELAY_MS.toLong())
            isLoaderVisible = true
        } else {
            isLoaderVisible = false
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isLoaderVisible) 0f else 1f,
        animationSpec = tween(durationMillis = LOADER_FADE_DURATION_MS),
        label = "primary_button_text_alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 62.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                },
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(textAlpha),
        )

        AnimatedVisibility(
            visible = isLoaderVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = LOADER_FADE_DURATION_MS)),
            exit = fadeOut(animationSpec = tween(durationMillis = LOADER_FADE_DURATION_MS)),
        ) {
            EterLoadingIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                dotSize = 8.dp,
                spacing = 6.dp,
            )
        }
    }
}
