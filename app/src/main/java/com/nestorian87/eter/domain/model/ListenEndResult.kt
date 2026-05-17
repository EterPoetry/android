package com.nestorian87.eter.domain.model

data class ListenEndResult(
    val listenedMs: Int,
    val isSuspicious: Boolean,
    val suspiciousReason: String? = null,
    val counted: Boolean,
    val countedAt: String? = null,
    val thresholdReached: Boolean,
)
