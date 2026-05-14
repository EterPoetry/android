package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePostRequestDto(
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val originAuthorName: String? = null,
    val categoryIds: List<Long>? = null,
)
