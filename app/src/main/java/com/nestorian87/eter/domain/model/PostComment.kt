package com.nestorian87.eter.domain.model

data class PostComment(
    val commentId: Long,
    val postId: Long? = null,
    val parentCommentId: Long? = null,
    val authorId: Long? = null,
    val author: PostAuthor? = null,
    val text: String? = null,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isLikedByAuthor: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
