package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListenProgressRequestDto(
    val token: String,
    val positionMs: Int,
)
