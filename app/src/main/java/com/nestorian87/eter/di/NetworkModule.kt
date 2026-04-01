package com.nestorian87.eter.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nestorian87.eter.BuildConfig
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.data.remote.auth.AuthCookieJar
import com.nestorian87.eter.data.remote.auth.AuthHeaderInterceptor
import com.nestorian87.eter.data.remote.auth.RefreshTokenAuthenticator
import com.nestorian87.eter.di.qualifier.RefreshAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.CookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideCookieJar(
        cookieJar: AuthCookieJar,
    ): CookieJar = cookieJar

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context,
    ): ChuckerInterceptor = ChuckerInterceptor.Builder(context)
        .collector(ChuckerCollector(context))
        .maxContentLength(250_000L)
        .redactHeaders("Authorization", "Cookie", "Set-Cookie")
        .alwaysReadResponseBody(false)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        authHeaderInterceptor: AuthHeaderInterceptor,
        refreshTokenAuthenticator: RefreshTokenAuthenticator,
        cookieJar: CookieJar,
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(chuckerInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(refreshTokenAuthenticator)
        .build()

    @Provides
    @Singleton
    @RefreshAuthApi
    fun provideRefreshAuthApi(
        loggingInterceptor: HttpLoggingInterceptor,
        cookieJar: CookieJar,
        json: Json,
    ): Api {
        val refreshClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(refreshClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(Api::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(
        retrofit: Retrofit,
    ): Api = retrofit.create(Api::class.java)
}
