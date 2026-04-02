package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.local.datastore.AuthSessionStore
import com.nestorian87.eter.data.mapper.toAuthUser
import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.data.remote.auth.AuthCookieJar
import com.nestorian87.eter.data.remote.auth.RefreshTokenCoordinator
import com.nestorian87.eter.data.remote.dto.EmailVerificationRequestDto
import com.nestorian87.eter.data.remote.dto.ForgotPasswordRequestDto
import com.nestorian87.eter.data.remote.dto.GoogleMobileAuthRequestDto
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.data.remote.dto.RegisterRequestDto
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.model.AuthSession
import com.nestorian87.eter.domain.model.EmailVerificationStatus
import com.nestorian87.eter.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

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
        val authSession = executeAuthRequest(
            httpErrorMapper = { statusCode ->
                when (statusCode) {
                    400, 401 -> AuthException.Reason.INVALID_CREDENTIALS
                    else -> AuthException.Reason.UNKNOWN
                }
            },
        ) {
            api.login(
                request = LoginRequestDto(
                    email = email.trim(),
                    password = password,
                ),
            ).toDomain()
        }
        sessionStore.saveSession(authSession)
        return authSession
    }

    override suspend fun loginWithGoogle(idToken: String): AuthSession {
        awaitInitialization()
        val authSession = executeAuthRequest(
            httpErrorMapper = { statusCode ->
                when (statusCode) {
                    400, 401 -> AuthException.Reason.GOOGLE_AUTH_FAILED
                    else -> AuthException.Reason.UNKNOWN
                }
            },
        ) {
            api.loginWithGoogle(
                request = GoogleMobileAuthRequestDto(
                    idToken = idToken,
                ),
            ).toDomain()
        }
        sessionStore.saveSession(authSession)
        return authSession
    }

    override suspend fun register(name: String, email: String, password: String): AuthSession {
        awaitInitialization()
        val authSession = executeAuthRequest(
            httpErrorMapper = { statusCode ->
                when (statusCode) {
                    409 -> AuthException.Reason.EMAIL_ALREADY_EXISTS
                    400 -> AuthException.Reason.INVALID_REGISTRATION_DATA
                    else -> AuthException.Reason.UNKNOWN
                }
            },
        ) {
            api.register(
                request = RegisterRequestDto(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                ),
            ).toDomain()
        }
        sessionStore.saveSession(authSession)
        return authSession
    }

    override suspend fun getEmailVerificationStatus(): EmailVerificationStatus {
        awaitInitialization()
        return executeAuthRequest {
            EmailVerificationStatus(
                remainingMs = api.getEmailVerificationStatus().remainingMs,
            )
        }
    }

    override suspend fun requestEmailVerificationCode(): EmailVerificationStatus {
        awaitInitialization()
        return executeAuthRequest(
            httpErrorMapper = { statusCode ->
                when (statusCode) {
                    429 -> AuthException.Reason.EMAIL_VERIFICATION_RATE_LIMIT
                    else -> AuthException.Reason.UNKNOWN
                }
            },
        ) {
            api.requestEmailVerificationCode()
            EmailVerificationStatus(
                remainingMs = api.getEmailVerificationStatus().remainingMs,
            )
        }
    }

    override suspend fun verifyEmail(email: String, code: String) {
        awaitInitialization()
        executeAuthRequest(
            httpErrorMapper = { statusCode ->
                when (statusCode) {
                    400, 401 -> AuthException.Reason.INVALID_VERIFICATION_CODE
                    else -> AuthException.Reason.UNKNOWN
                }
            },
        ) {
            api.verifyEmail(
                request = EmailVerificationRequestDto(
                    email = email.trim(),
                    code = code,
                ),
            )
        }
        refreshCurrentUser()
    }

    override suspend fun requestPasswordReset(email: String) {
        awaitInitialization()
        executeAuthRequest {
            api.requestPasswordReset(
                request = ForgotPasswordRequestDto(
                    email = email.trim(),
                ),
            )
        }
    }

    override suspend fun refreshCurrentUser(): AuthSession? {
        awaitInitialization()
        val currentSession = session.value ?: return null

        val updatedUser = executeAuthRequest {
            api.getProfileMe().toAuthUser()
        }

        val updatedSession = currentSession.copy(user = updatedUser)
        sessionStore.saveSession(updatedSession)
        return updatedSession
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
