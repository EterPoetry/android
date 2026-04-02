package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileMeDto(
    val userId: Long,
    val name: String,
    val email: String,
    val photo: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
)
