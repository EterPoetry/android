package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponseDto(
    val message: String? = null,
    val errors: List<ApiFieldErrorDto> = emptyList(),
)

@Serializable
data class ApiFieldErrorDto(
    val code: String? = null,
    val field: String? = null,
    val message: String? = null,
)
