package com.nestorian87.eter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val userId: Long,
    val name: String,
    val email: String,
    val photo: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: String,
)
