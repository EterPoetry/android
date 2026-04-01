package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.local.datastore.AuthSessionStore
import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.data.remote.auth.AuthCookieJar
import com.nestorian87.eter.data.remote.auth.RefreshTokenCoordinator
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.model.AuthSession
import com.nestorian87.eter.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: Api,
    private val sessionStore: AuthSessionStore,
    private val cookieJar: AuthCookieJar,
    private val refreshTokenCoordinator: RefreshTokenCoordinator,
    applicationScope: CoroutineScope,
) : AuthRepository {
    private val initialization = CompletableDeferred<Unit>()

    init {
        applicationScope.launch {
            runCatching {
                sessionStore.initialize()
                cookieJar.initialize()
            }.onSuccess {
                initialization.complete(Unit)
            }.onFailure { error ->
                initialization.completeExceptionally(error)
            }
        }
    }

    override val session: StateFlow<AuthSession?> = sessionStore.session

    override suspend fun login(email: String, password: String): AuthSession {
        awaitInitialization()
        val authSession = try {
            api.login(
                request = LoginRequestDto(
                    email = email.trim(),
                    password = password,
                ),
            ).toDomain()
        } catch (error: HttpException) {
            throw when (error.code()) {
                400, 401 -> AuthException(AuthException.Reason.INVALID_CREDENTIALS, error)
                else -> AuthException(AuthException.Reason.UNKNOWN, error)
            }
        } catch (error: IOException) {
            throw AuthException(AuthException.Reason.NETWORK, error)
        }
        sessionStore.saveSession(authSession)
        return authSession
    }

    override suspend fun logout() {
        awaitInitialization()
        runCatching { api.logout() }
        sessionStore.clearSession()
        cookieJar.clearAll()
    }

    override suspend fun restoreSession(): Boolean {
        awaitInitialization()

        if (sessionStore.accessToken != null) {
            return true
        }

        if (!cookieJar.hasRefreshCookie()) {
            return false
        }

        return refreshTokenCoordinator.refreshAccessToken(failedToken = null) != null
    }

    private suspend fun awaitInitialization() {
        initialization.await()
    }
}
