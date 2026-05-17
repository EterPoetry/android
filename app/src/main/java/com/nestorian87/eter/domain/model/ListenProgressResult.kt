package com.nestorian87.eter.domain.model

data class ListenProgressResult(
    val listenedMs: Int,
    val isSuspicious: Boolean,
    val suspiciousReason: String? = null,
)
