package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostFeedResponseDto(
    val items: List<PostDto>,
    val nextCursor: String? = null,
    val hasMore: Boolean,
)
