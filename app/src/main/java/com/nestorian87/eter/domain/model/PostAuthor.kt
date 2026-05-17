package com.nestorian87.eter.domain.model

data class PostAuthor(
    val userId: Long,
    val name: String,
    val username: String,
    val photo: String? = null,
    val isPremium: Boolean,
)
