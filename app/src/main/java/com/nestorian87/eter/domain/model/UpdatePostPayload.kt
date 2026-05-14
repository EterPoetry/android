package com.nestorian87.eter.domain.model

data class UpdatePostPayload(
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val originAuthorName: String? = null,
    val categoryIds: List<Long>? = null,
)
