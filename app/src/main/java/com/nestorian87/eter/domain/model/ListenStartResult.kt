package com.nestorian87.eter.domain.model

data class ListenStartResult(
    val token: String,
    val listenedMs: Int,
    val trackDurationMs: Int,
    val isSuspicious: Boolean,
)
