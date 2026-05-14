package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostCategoryDto(
    val categoryId: Long,
    val categoryName: String,
    val categoryDescription: String? = null,
)
