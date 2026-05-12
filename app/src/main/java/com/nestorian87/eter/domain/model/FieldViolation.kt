package com.nestorian87.eter.domain.model

data class FieldViolation(
    val field: String,
    val code: String,
    val message: String? = null,
)
