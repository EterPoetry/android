package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommentLikeResponseDto(
    val ok: Boolean,
    val likesCount: Int,
)
