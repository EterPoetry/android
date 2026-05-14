package com.nestorian87.eter.data.remote.api

import com.nestorian87.eter.data.remote.dto.AuthResponseDto
import com.nestorian87.eter.data.remote.dto.DeletePostResponseDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationRequestDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationStatusDto
import com.nestorian87.eter.data.remote.dto.ForgotPasswordRequestDto
import com.nestorian87.eter.data.remote.dto.GoogleMobileAuthRequestDto
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.data.remote.dto.MyPostsResponseDto
import com.nestorian87.eter.data.remote.dto.PostCategoryDto
import com.nestorian87.eter.data.remote.dto.PostDto
import com.nestorian87.eter.data.remote.dto.ProfileMeDto
import com.nestorian87.eter.data.remote.dto.PublicConfigDto
import com.nestorian87.eter.data.remote.dto.RegisterRequestDto
import com.nestorian87.eter.data.remote.dto.UpdatePostRequestDto
import com.nestorian87.eter.data.remote.auth.NoAuth
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @NoAuth
    @GET("config")
    suspend fun getPublicConfig(): PublicConfigDto

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

    @GET("posts/categories")
    suspend fun getCategories(
        @Query("search") search: String? = null,
    ): List<PostCategoryDto>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part audio: MultipartBody.Part,
    ): PostDto

    @GET("posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Long,
    ): PostDto

    @PATCH("posts/{postId}")
    suspend fun updatePost(
        @Path("postId") postId: Long,
        @Body request: UpdatePostRequestDto,
    ): PostDto

    @Multipart
    @PATCH("posts/{postId}/audio")
    suspend fun replacePostAudio(
        @Path("postId") postId: Long,
        @Part audio: MultipartBody.Part,
    ): PostDto

    @GET("posts/me")
    suspend fun getMyPosts(
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): MyPostsResponseDto

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Long,
    ): DeletePostResponseDto

    @NoAuth
    @POST("auth/logout")
    suspend fun logout()
}
