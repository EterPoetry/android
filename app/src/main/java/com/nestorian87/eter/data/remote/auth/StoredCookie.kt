package com.nestorian87.eter.data.remote.auth

import kotlinx.serialization.Serializable

@Serializable
data class StoredCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean,
    val hostOnly: Boolean,
    val persistent: Boolean,
)
