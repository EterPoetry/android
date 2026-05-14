package com.nestorian87.eter.domain.model

data class MyPostsQuery(
    val search: String? = null,
    val sortBy: MyPostsSortBy? = null,
    val sortOrder: SortOrder? = null,
    val offset: Int? = null,
    val limit: Int? = null,
)

enum class MyPostsSortBy {
    CREATED_AT,
    UPDATED_AT,
    TITLE,
    LISTENS,
}

enum class SortOrder {
    ASC,
    DESC,
}
