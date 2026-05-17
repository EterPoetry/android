package com.nestorian87.eter.domain.model

data class FollowProfilesPage(
    val items: List<PublicProfile>,
    val total: Int,
    val limit: Int,
    val nextCursor: String? = null,
    val hasMore: Boolean,
)
