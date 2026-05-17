package com.nestorian87.eter.domain.model

data class PostListPage(
    val items: List<Post>,
    val total: Int,
    val offset: Int,
)
