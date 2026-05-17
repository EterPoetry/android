package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListenEndRequestDto(
    val token: String,
    val positionMs: Int,
    val sessionId: String? = null,
)
