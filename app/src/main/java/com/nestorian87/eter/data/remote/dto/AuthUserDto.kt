package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthUserDto(
    val userId: Long,
    val name: String,
    val email: String,
    val googleId: String? = null,
    val resetPasswordTokenHash: String? = null,
    val resetPasswordExpiresAt: String? = null,
    val photo: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: String,
)
