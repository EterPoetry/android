package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.AuthSession
import com.nestorian87.eter.domain.model.EmailVerificationStatus
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<AuthSession?>

    suspend fun login(email: String, password: String): AuthSession

    suspend fun register(name: String, email: String, password: String): AuthSession

    suspend fun getEmailVerificationStatus(): EmailVerificationStatus

    suspend fun requestEmailVerificationCode(): EmailVerificationStatus

    suspend fun verifyEmail(email: String, code: String)

    suspend fun refreshCurrentUser(): AuthSession?

    suspend fun logout()

    suspend fun restoreSession(): Boolean
}
