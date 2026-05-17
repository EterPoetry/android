package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostTextSynchronizationDto(
    val lineIndex: Int,
    val audioStartMomentMs: Int,
)
