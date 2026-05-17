package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PostAuthorDto(
    val userId: Long,
    val name: String,
    val username: String,
    val photo: String? = null,
    val isPremium: Boolean,
)
