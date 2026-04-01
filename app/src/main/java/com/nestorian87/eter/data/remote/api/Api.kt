package com.nestorian87.eter.data.remote.api

import com.nestorian87.eter.data.remote.dto.AuthResponseDto
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.data.remote.auth.NoAuth
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {

    @NoAuth
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): AuthResponseDto

    @NoAuth
    @POST("auth/refresh")
    suspend fun refresh(): AuthResponseDto

    @NoAuth
    @POST("auth/logout")
    suspend fun logout()
}
