package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val user: AuthUserDto,
    val accessToken: String,
)
