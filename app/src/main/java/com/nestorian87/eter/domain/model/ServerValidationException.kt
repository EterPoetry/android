package com.nestorian87.eter.domain.model

class ServerValidationException(
    val reason: Reason,
    val fieldViolations: Set<FieldViolation> = emptySet(),
    cause: Throwable? = null,
) : Exception(cause) {
    enum class Reason {
        INVALID_DATA,
        CONFLICT,
    }
}
