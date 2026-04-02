package com.nestorian87.eter.domain.model

data class EmailVerificationStatus(
    val remainingMs: Long? = null,
) {
    val canRequestCode: Boolean
        get() = remainingMs == null
}
