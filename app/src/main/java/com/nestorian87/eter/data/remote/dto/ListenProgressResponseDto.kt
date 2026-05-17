package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListenProgressResponseDto(
    val listenedMs: Int,
    val isSuspicious: Boolean,
    val suspiciousReason: String? = null,
)
