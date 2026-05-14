package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionConfigDto(
    val priceUsd: Int,
)
