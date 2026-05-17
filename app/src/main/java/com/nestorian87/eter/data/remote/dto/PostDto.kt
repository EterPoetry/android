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
    val audioDurationSeconds: Int? = null,
    val status: PostStatusDto,
    val listens: Int,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val originAuthorName: String? = null,
    val textSynchronization: List<PostTextSynchronizationDto> = emptyList(),
    val categories: List<PostCategoryDto> = emptyList(),
    val authorId: Long,
    val author: PostAuthorDto? = null,
    val createdAt: String,
    val updatedAt: String,
)
