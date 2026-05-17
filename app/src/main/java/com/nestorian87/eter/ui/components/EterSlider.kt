package com.nestorian87.eter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

@Composable
fun EterSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val ringColor = MaterialTheme.colorScheme.surface
    val thumbRadiusPx = 10.dp
    val ringThicknessPx = 4.dp

    var widthPx by remember { mutableFloatStateOf(0f) }

    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    val totalRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        thumbRadiusPx.toPx() + ringThicknessPx.toPx()
    }

    fun positionToValue(x: Float): Float {
        if (widthPx <= 0f) return valueRange.start
        val trackStartX = totalRadiusPx
        val trackWidth = (widthPx - totalRadiusPx * 2f).coerceAtLeast(0f)
        if (trackWidth <= 0f) return valueRange.start
        val trackFraction = ((x - trackStartX) / trackWidth).coerceIn(0f, 1f)
        return valueRange.start + trackFraction * (valueRange.endInclusive - valueRange.start)
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    onValueChange(positionToValue(offset.x))
                    onValueChangeFinished?.invoke()
                }
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = { onValueChangeFinished?.invoke() },
                    onDragCancel = { onValueChangeFinished?.invoke() },
                ) { change, _ ->
                    onValueChange(positionToValue(change.position.x))
                    change.consume()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackHeight = 5.dp.toPx()
            val thumbRadius = thumbRadiusPx.toPx()
            val ringThickness = ringThicknessPx.toPx()
            val totalRadius = thumbRadius + ringThickness
            val trackStartX = totalRadius
            val trackWidth = (size.width - totalRadius * 2).coerceAtLeast(0f)
            val centerY = size.height / 2f
            val thumbX = trackStartX + trackWidth * fraction

            drawRoundRect(
                color = inactiveColor,
                topLeft = Offset(trackStartX, centerY - trackHeight / 2),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2),
            )
            if (fraction > 0f) {
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(trackStartX, centerY - trackHeight / 2),
                    size = Size(trackWidth * fraction, trackHeight),
                    cornerRadius = CornerRadius(trackHeight / 2),
                )
            }
            drawCircle(color = ringColor, radius = totalRadius, center = Offset(thumbX, centerY))
            drawCircle(color = primaryColor, radius = thumbRadius, center = Offset(thumbX, centerY))
        }
    }
}
