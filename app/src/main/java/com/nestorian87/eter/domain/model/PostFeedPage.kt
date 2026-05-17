package com.nestorian87.eter.domain.model

data class PostFeedPage(
    val items: List<Post>,
    val nextCursor: String?,
    val hasMore: Boolean,
)
