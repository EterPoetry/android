package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PopularPostsResponseDto(
    val items: List<PostDto> = emptyList(),
    val total: Int,
    val snapshotId: String,
    val snapshotGeneratedAt: String,
    val nextCursor: String? = null,
    val hasMore: Boolean,
)
