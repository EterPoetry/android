package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostCommentsResponseDto(
    val items: List<PostCommentDto> = emptyList(),
    val nextCursor: String? = null,
)
