package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostCommentDto(
    val commentId: Long,
    val postId: Long? = null,
    @SerialName("replyToCommentId")
    val parentCommentId: Long? = null,
    val authorId: Long? = null,
    val author: PostAuthorDto? = null,
    @SerialName("commentText")
    val text: String? = null,
    val likesCount: Int = 0,
    val repliesCount: Int = 0,
    @SerialName("isLiked")
    val isLikedByMe: Boolean = false,
    val isLikedByAuthor: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
