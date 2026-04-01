package com.nestorian87.eter.data.remote.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

@Singleton
class AuthCookieJar @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val applicationScope: CoroutineScope,
    private val json: Json,
) : CookieJar {
    private val cookieCache = ConcurrentHashMap<String, Cookie>()
    private val initializationMutex = Mutex()
    @Volatile
    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return
        initializationMutex.withLock {
            if (isInitialized) return
            val restoredCookies = dataStore.data
                .catch { emit(emptyPreferences()) }
                .first()[CookieKey]
                .orEmpty()
                .mapNotNull(::decodeCookie)
            restoredCookies.forEach { cookie ->
                cookieCache[cookie.cacheKey()] = cookie
            }
            pruneExpiredCookies()
            isInitialized = true
        }
    }

    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        var changed = false
        cookies.forEach { cookie ->
            if (cookie.expiresAt < System.currentTimeMillis()) {
                changed = cookieCache.remove(cookie.cacheKey()) != null || changed
            } else {
                cookieCache[cookie.cacheKey()] = cookie
                changed = true
            }
        }
        if (pruneExpiredCookies() || changed) {
            persistCookies()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val changed = pruneExpiredCookies()
        if (changed) {
            persistCookies()
        }
        return cookieCache.values.toList().filter { it.matches(url) }
    }

    fun hasRefreshCookie(): Boolean {
        val now = System.currentTimeMillis()
        return cookieCache.values.toList().any { cookie ->
            cookie.name == REFRESH_TOKEN_COOKIE_NAME && cookie.expiresAt > now
        }
    }

    fun clearAll() {
        cookieCache.clear()
        persistCookies()
    }

    private fun pruneExpiredCookies(): Boolean {
        val before = cookieCache.size
        val now = System.currentTimeMillis()
        cookieCache.entries.removeIf { (_, cookie) -> cookie.expiresAt < now }
        return before != cookieCache.size
    }

    private fun persistCookies() {
        val serializedCookies = cookieCache.values
            .toList()
            .map { cookie -> json.encodeToString(StoredCookie.serializer(), cookie.toStoredCookie()) }
            .toSet()
        applicationScope.launch {
            dataStore.edit { preferences ->
                preferences[CookieKey] = serializedCookies
            }
        }
    }

    private fun Cookie.cacheKey(): String = "$name|$domain|$path"

    private fun Cookie.toStoredCookie(): StoredCookie = StoredCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        secure = secure,
        httpOnly = httpOnly,
        hostOnly = hostOnly,
        persistent = persistent,
    )

    private fun decodeCookie(encoded: String): Cookie? = runCatching {
        val storedCookie = json.decodeFromString(StoredCookie.serializer(), encoded)
        Cookie.Builder()
            .name(storedCookie.name)
            .value(storedCookie.value)
            .apply {
                if (storedCookie.hostOnly) {
                    hostOnlyDomain(storedCookie.domain)
                } else {
                    domain(storedCookie.domain)
                }
                path(storedCookie.path)
                if (storedCookie.persistent) {
                    expiresAt(storedCookie.expiresAt)
                }
                if (storedCookie.secure) secure()
                if (storedCookie.httpOnly) httpOnly()
            }
            .build()
    }.getOrNull()

    private companion object {
        val CookieKey = stringSetPreferencesKey("auth_cookies")
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }
}
