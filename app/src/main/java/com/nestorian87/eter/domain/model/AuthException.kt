package com.nestorian87.eter.domain.model

class AuthException(
    val reason: Reason,
    cause: Throwable? = null,
) : Exception(cause) {

    enum class Reason {
        INVALID_CREDENTIALS,
        INVALID_REGISTRATION_DATA,
        EMAIL_ALREADY_EXISTS,
        NETWORK,
        UNKNOWN,
    }
}
