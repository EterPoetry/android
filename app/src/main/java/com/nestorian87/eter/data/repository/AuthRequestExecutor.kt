package com.nestorian87.eter.data.repository

import com.nestorian87.eter.domain.model.AuthException
import java.io.IOException
import retrofit2.HttpException

suspend inline fun <T> executeAuthRequest(
    crossinline httpErrorMapper: (error: HttpException) -> Throwable = {
        AuthException(
            reasons = setOf(AuthException.Reason.UNKNOWN),
            cause = it,
        )
    },
    crossinline block: suspend () -> T,
): T {
    try {
        return block()
    } catch (error: IOException) {
        throw AuthException(
            reasons = setOf(AuthException.Reason.NETWORK),
            cause = error,
        )
    } catch (error: HttpException) {
        throw httpErrorMapper(error)
    }
}
