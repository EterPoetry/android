package com.nestorian87.eter.ui.components

fun Int?.toDurationText(): String {
    val totalSeconds = (this ?: 0).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
