package com.nestorian87.eter.data.remote.api

import com.nestorian87.eter.data.remote.dto.AuthResponseDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationRequestDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationStatusDto
import com.nestorian87.eter.data.remote.dto.ForgotPasswordRequestDto
import com.nestorian87.eter.data.remote.dto.GoogleMobileAuthRequestDto
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.data.remote.dto.ProfileMeDto
import com.nestorian87.eter.data.remote.dto.RegisterRequestDto
import com.nestorian87.eter.data.remote.auth.NoAuth
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {

    @NoAuth
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): AuthResponseDto

    @NoAuth
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto,
    ): AuthResponseDto

    @NoAuth
    @POST("auth/google/mobile")
    suspend fun loginWithGoogle(
        @Body request: GoogleMobileAuthRequestDto,
    ): AuthResponseDto

    @NoAuth
    @POST("auth/refresh")
    suspend fun refresh(): AuthResponseDto

    @GET("auth/email/verify/request")
    suspend fun getEmailVerificationStatus(): EmailVerificationStatusDto

    @POST("auth/email/verify/request")
    suspend fun requestEmailVerificationCode()

    @NoAuth
    @POST("auth/email/verify")
    suspend fun verifyEmail(
        @Body request: EmailVerificationRequestDto,
    )

    @NoAuth
    @POST("auth/password/forgot")
    suspend fun requestPasswordReset(
        @Body request: ForgotPasswordRequestDto,
    )

    @GET("profile/me")
    suspend fun getProfileMe(): ProfileMeDto

    @NoAuth
    @POST("auth/logout")
    suspend fun logout()
}
