package com.nestorian87.eter.data.remote.auth

import com.nestorian87.eter.data.local.datastore.AuthSessionStore
import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.di.qualifier.RefreshAuthApi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

@Singleton
class RefreshTokenCoordinator @Inject constructor(
    @param:RefreshAuthApi private val api: Api,
    private val sessionStore: AuthSessionStore,
    private val cookieJar: AuthCookieJar,
) {
    private val refreshMutex = Mutex()

    suspend fun refreshAccessToken(failedToken: String?): String? = refreshMutex.withLock {
        val currentToken = sessionStore.accessToken
        if (!currentToken.isNullOrBlank() && failedToken != null && currentToken != failedToken) {
            return currentToken
        }

        if (!cookieJar.hasRefreshCookie()) {
            sessionStore.clearSession()
            return null
        }

        val refreshedSession = try {
            api.refresh().toDomain()
        } catch (error: HttpException) {
            if (error.code() == 400 || error.code() == 401 || error.code() == 403) {
                sessionStore.clearSession()
                cookieJar.clearAll()
            }
            null
        } catch (_: IOException) {
            null
        } ?: return null

        return try {
            sessionStore.saveSession(refreshedSession)
            refreshedSession.accessToken
        } catch (_: IOException) {
            refreshedSession.accessToken
        }
    }
}
