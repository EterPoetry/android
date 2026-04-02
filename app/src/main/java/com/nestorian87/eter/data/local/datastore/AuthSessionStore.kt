package com.nestorian87.eter.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nestorian87.eter.domain.model.AuthSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

@Singleton
class AuthSessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    private val sessionState = MutableStateFlow<AuthSession?>(null)
    private val initializationMutex = Mutex()
    @Volatile
    private var isInitialized = false

    val session: StateFlow<AuthSession?> = sessionState.asStateFlow()

    val accessToken: String?
        get() = sessionState.value?.accessToken

    suspend fun initialize() {
        if (isInitialized) {
            return
        }
        initializationMutex.withLock {
            if (isInitialized) {
                return
            }
            sessionState.value = readPersistedSession()
            isInitialized = true
        }
    }

    suspend fun saveSession(authSession: AuthSession) {
        sessionState.value = authSession
        dataStore.edit { preferences ->
            preferences[SessionKey] = json.encodeToString(AuthSession.serializer(), authSession)
        }
        isInitialized = true
    }

    suspend fun clearSession() {
        sessionState.value = null
        dataStore.edit { preferences ->
            preferences.remove(SessionKey)
        }
        isInitialized = true
    }

    private suspend fun readPersistedSession(): AuthSession? =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[SessionKey] }
            .first()
            ?.let { encoded ->
                runCatching {
                    json.decodeFromString(AuthSession.serializer(), encoded)
                }.getOrNull()
            }

    private companion object {
        val SessionKey = stringPreferencesKey("auth_session")
    }
}
