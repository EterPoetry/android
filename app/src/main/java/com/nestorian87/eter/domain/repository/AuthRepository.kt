package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val session: StateFlow<AuthSession?>

    suspend fun login(email: String, password: String): AuthSession

    suspend fun logout()

    suspend fun restoreSession(): Boolean
}
