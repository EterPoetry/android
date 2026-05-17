package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostListResponseDto(
    val items: List<PostDto>,
    val total: Int,
    val offset: Int,
)
