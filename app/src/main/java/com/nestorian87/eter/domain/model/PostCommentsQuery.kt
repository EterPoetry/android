package com.nestorian87.eter.domain.model

data class PostCommentsQuery(
    val limit: Int? = null,
    val cursor: String? = null,
    val sort: PostCommentsSort? = null,
)

enum class PostCommentsSort {
    NEWEST,
    OLDEST,
    POPULAR,
}
