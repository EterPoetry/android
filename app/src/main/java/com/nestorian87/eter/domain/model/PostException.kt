package com.nestorian87.eter.domain.model

class PostException(
    val reasons: Set<Reason>,
    cause: Throwable? = null,
) : Exception(cause) {
    init {
        require(reasons.isNotEmpty()) { "PostException requires at least one reason" }
    }

    val primaryReason: Reason
        get() = reasons.first()

    enum class Reason {
        NETWORK,
        AUDIO_DURATION_LIMIT_EXCEEDED,
        POST_IS_STILL_PROCESSING,
        AUDIO_REPLACEMENT_NOT_ALLOWED,
        NOT_FOUND,
        FORBIDDEN,
        UNKNOWN,
    }
}
