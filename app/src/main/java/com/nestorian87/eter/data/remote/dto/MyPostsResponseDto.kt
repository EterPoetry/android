package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MyPostsResponseDto(
    val items: List<PostDto> = emptyList(),
    val total: Int,
    val offset: Int,
)
