package com.nestorian87.eter.domain.model

data class PostCategory(
    val categoryId: Long,
    val categoryName: String,
    val categoryDescription: String? = null,
)
