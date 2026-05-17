package com.nestorian87.eter.data.repository

data class PostInteractionOverride(
    val likesCount: Int? = null,
    val isLiked: Boolean? = null,
    val listens: Int? = null,
    val isLikePending: Boolean = false,
)
