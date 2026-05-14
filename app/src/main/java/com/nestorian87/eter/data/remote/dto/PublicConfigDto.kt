package com.nestorian87.eter.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PublicConfigDto(
    val recording: RecordingConfigDto,
    val subscription: SubscriptionConfigDto,
)
