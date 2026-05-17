package com.nestorian87.eter.domain.model

data class FollowProfilesQuery(
    val search: String? = null,
    val limit: Int? = null,
    val cursor: String? = null,
)
