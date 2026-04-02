package com.nestorian87.eter.data.repository

import com.nestorian87.eter.domain.model.AuthException
import java.io.IOException
import retrofit2.HttpException

suspend inline fun <T> executeAuthRequest(
    crossinline httpErrorMapper: (statusCode: Int) -> AuthException.Reason = { AuthException.Reason.UNKNOWN },
    crossinline block: suspend () -> T,
): T {
    try {
        return block()
    } catch (error: IOException) {
        throw AuthException(AuthException.Reason.NETWORK, error)
    } catch (error: HttpException) {
        throw AuthException(httpErrorMapper(error.code()), error)
    }
}
