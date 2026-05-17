package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCommentRequestDto(
    val commentText: String,
    val replyToCommentId: Long? = null,
)
