package com.nestorian87.eter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EditPostDraftCategory(
    val categoryId: Long,
    val categoryName: String,
)
