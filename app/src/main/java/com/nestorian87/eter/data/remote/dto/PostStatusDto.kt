package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class PostStatusDto {
    draft,
    processing,
    published,
}
