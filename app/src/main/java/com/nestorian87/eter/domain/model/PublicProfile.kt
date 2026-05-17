package com.nestorian87.eter.domain.model

data class PublicProfile(
    val userId: Long,
    val name: String,
    val username: String,
    val photo: String? = null,
    val isPremium: Boolean,
    val isSubscribed: Boolean,
    val createdAt: String,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
)
