package com.nestorian87.eter.domain.model

data class Post(
    val postId: Long,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val audioFileName: String? = null,
    val audioFileUrl: String? = null,
    val status: PostStatus,
    val listens: Int,
    val originAuthorName: String? = null,
    val categories: List<PostCategory> = emptyList(),
    val authorId: Long,
    val createdAt: String,
    val updatedAt: String,
)
