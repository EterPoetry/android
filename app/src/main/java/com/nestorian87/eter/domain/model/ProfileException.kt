@file:Suppress("unused")

package com.nestorian87.eter.domain.model

class ProfileException(
    val reasons: Set<Reason>,
    cause: Throwable? = null,
) : Exception(cause) {
    init {
        require(reasons.isNotEmpty()) { "ProfileException requires at least one reason" }
    }

    val primaryReason: Reason
        get() = reasons.first()

    enum class Reason {
        NETWORK,
        UNAUTHORIZED,
        USER_NOT_FOUND,
        SELF_FOLLOW_NOT_ALLOWED,
        SELF_UNFOLLOW_NOT_ALLOWED,
        INVALID_DATA,
        UNKNOWN,
    }
}
