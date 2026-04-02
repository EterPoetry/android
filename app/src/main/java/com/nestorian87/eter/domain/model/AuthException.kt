package com.nestorian87.eter.domain.model

class AuthException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(cause) {

    enum class Reason {
        INVALID_CREDENTIALS,
        GOOGLE_AUTH_FAILED,
        INVALID_REGISTRATION_DATA,
        INVALID_VERIFICATION_CODE,
        EMAIL_VERIFICATION_RATE_LIMIT,
        EMAIL_ALREADY_EXISTS,
        NETWORK,
        UNKNOWN,
    }
}
