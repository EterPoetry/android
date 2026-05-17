package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListenEndResponseDto(
    val listenedMs: Int,
    val isSuspicious: Boolean,
    val suspiciousReason: String? = null,
    val counted: Boolean,
    val countedAt: String? = null,
    val thresholdReached: Boolean,
)
