package com.nestorian87.eter.domain.model

import java.io.File

data class AudioDraft(
    val file: File,
    val displayName: String? = null,
    val durationMs: Long,
    val recordedAtEpochMs: Long? = null,
)
