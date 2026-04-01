package com.nestorian87.eter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val user: AuthUser,
    val accessToken: String,
)
