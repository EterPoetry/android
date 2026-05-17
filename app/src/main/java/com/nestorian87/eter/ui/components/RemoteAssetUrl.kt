package com.nestorian87.eter.ui.components

import com.nestorian87.eter.BuildConfig

internal fun String?.resolveRemoteAssetUrl(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null
    if (
        value.startsWith("http://", ignoreCase = true) ||
        value.startsWith("https://", ignoreCase = true) ||
        value.startsWith("content://", ignoreCase = true) ||
        value.startsWith("file://", ignoreCase = true)
    ) {
        return value
    }
    return "${BuildConfig.API_BASE_URL.removeSuffix("/")}/${value.removePrefix("/")}"
}
