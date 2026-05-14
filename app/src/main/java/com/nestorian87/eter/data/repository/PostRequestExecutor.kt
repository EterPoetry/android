package com.nestorian87.eter.data.repository

import com.nestorian87.eter.domain.model.PostException
import java.io.IOException
import retrofit2.HttpException

suspend inline fun <T> executePostRequest(
    crossinline httpErrorMapper: (error: HttpException) -> Throwable = {
        PostException(
            reasons = setOf(PostException.Reason.UNKNOWN),
            cause = it,
        )
    },
    crossinline block: suspend () -> T,
): T {
    try {
        return block()
    } catch (error: IOException) {
        throw PostException(
            reasons = setOf(PostException.Reason.NETWORK),
            cause = error,
        )
    } catch (error: HttpException) {
        throw httpErrorMapper(error)
    }
}
