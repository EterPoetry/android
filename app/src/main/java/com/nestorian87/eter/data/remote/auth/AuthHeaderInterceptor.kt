package com.nestorian87.eter.data.remote.auth

import com.nestorian87.eter.data.local.datastore.AuthSessionStore
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

@Singleton
class AuthHeaderInterceptor @Inject constructor(
    private val sessionStore: AuthSessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val invocation = request.tag(Invocation::class.java)
        val hasNoAuth = invocation?.method()?.isAnnotationPresent(NoAuth::class.java) == true
        if (hasNoAuth) {
            return chain.proceed(request)
        }

        val accessToken = sessionStore.accessToken
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(request)
        }

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build(),
        )
    }
}
