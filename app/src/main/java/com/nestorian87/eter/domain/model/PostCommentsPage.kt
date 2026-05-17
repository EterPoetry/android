package com.nestorian87.eter.domain.model

data class PostCommentsPage(
    val items: List<PostComment>,
    val nextCursor: String? = null,
)
