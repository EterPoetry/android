package com.nestorian87.eter.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

internal fun Int.toCompactCountText(): String = toLong().toCompactCountText()

internal fun Long.toCompactCountText(): String {
    val absolute = abs(this)
    val divisor = when {
        absolute >= 1_000_000_000L -> 1_000_000_000.0 to "B"
        absolute >= 1_000_000L -> 1_000_000.0 to "M"
        absolute >= 1_000L -> 1_000.0 to "K"
        else -> return toString()
    }
    val value = this / divisor.first
    val pattern = if (abs(value) >= 100 || value % 1.0 == 0.0) "#0" else "#0.#"
    val formatter = DecimalFormat(
        pattern,
        DecimalFormatSymbols(Locale.US),
    )
    return "${formatter.format(value)}${divisor.second}"
}
