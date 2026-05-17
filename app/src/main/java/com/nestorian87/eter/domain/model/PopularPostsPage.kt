package com.nestorian87.eter.domain.model

data class PopularPostsPage(
    val items: List<Post>,
    val total: Int,
    val snapshotId: String,
    val snapshotGeneratedAt: String,
    val nextCursor: String?,
    val hasMore: Boolean,
)
