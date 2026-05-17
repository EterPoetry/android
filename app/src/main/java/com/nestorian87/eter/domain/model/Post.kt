package com.nestorian87.eter.domain.model

data class Post(
    val postId: Long,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val audioFileName: String? = null,
    val audioFileUrl: String? = null,
    val audioDurationSeconds: Int? = null,
    val status: PostStatus,
    val listens: Int,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val originAuthorName: String? = null,
    val textSynchronization: List<PostTextSynchronization> = emptyList(),
    val categories: List<PostCategory> = emptyList(),
    val authorId: Long,
    val author: PostAuthor? = null,
    val createdAt: String,
    val updatedAt: String,
)
