package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiFieldErrorDto(
    val code: String? = null,
    val field: String? = null,
    val message: String? = null,
)
