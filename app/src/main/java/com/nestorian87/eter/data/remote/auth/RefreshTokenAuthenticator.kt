package com.nestorian87.eter.data.remote.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class RefreshTokenAuthenticator @Inject constructor(
    private val refreshTokenCoordinator: RefreshTokenCoordinator,
) : Authenticator {

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (responseCount(response) > MAX_AUTH_RETRIES) {
            return null
        }

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        val newAccessToken = runBlocking {
            refreshTokenCoordinator.refreshAccessToken(failedToken)
        } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var currentResponse: Response? = response
        var result = 1
        while (currentResponse?.priorResponse != null) {
            result++
            currentResponse = currentResponse.priorResponse
        }
        return result
    }

    private companion object {
        const val MAX_AUTH_RETRIES = 1
    }
}
