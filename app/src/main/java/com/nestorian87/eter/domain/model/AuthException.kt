package com.nestorian87.eter.domain.model

class AuthException(
    val reasons: Set<Reason>,
    cause: Throwable? = null,
) : Exception(cause) {
    init {
        require(reasons.isNotEmpty()) { "AuthException requires at least one reason" }
    }

    val primaryReason: Reason
        get() = reasons.first()

    enum class Reason {
        INVALID_CREDENTIALS,
        GOOGLE_AUTH_FAILED,
        INVALID_VERIFICATION_CODE,
        EMAIL_VERIFICATION_RATE_LIMIT,
        NETWORK,
        UNKNOWN,
    }
}
