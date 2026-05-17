package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListenStartResponseDto(
    val token: String,
    val listenedMs: Int,
    val trackDurationMs: Int,
    val isSuspicious: Boolean,
)
