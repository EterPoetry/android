package com.nestorian87.eter.domain.model

data class PostSearchQuery(
    val search: String? = null,
    val categoryId: Long? = null,
    val sortBy: PostSearchSortBy = PostSearchSortBy.NEWEST,
    val offset: Int = 0,
    val limit: Int = 20,
)
