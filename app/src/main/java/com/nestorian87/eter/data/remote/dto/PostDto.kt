package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val postId: Long,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val audioFileName: String? = null,
    val audioFileUrl: String? = null,
    val status: PostStatusDto,
    val listens: Int,
    val originAuthorName: String? = null,
    val categories: List<PostCategoryDto> = emptyList(),
    val authorId: Long,
    val createdAt: String,
    val updatedAt: String,
)
