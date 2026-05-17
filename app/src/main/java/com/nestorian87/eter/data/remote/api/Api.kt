package com.nestorian87.eter.data.remote.api

import com.nestorian87.eter.data.remote.dto.AuthResponseDto
import com.nestorian87.eter.data.remote.dto.CommentLikeResponseDto
import com.nestorian87.eter.data.remote.dto.CreateCommentRequestDto
import com.nestorian87.eter.data.remote.dto.DeletePostResponseDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationRequestDto
import com.nestorian87.eter.data.remote.dto.EmailVerificationStatusDto
import com.nestorian87.eter.data.remote.dto.ForgotPasswordRequestDto
import com.nestorian87.eter.data.remote.dto.GoogleMobileAuthRequestDto
import com.nestorian87.eter.data.remote.dto.ListenEndRequestDto
import com.nestorian87.eter.data.remote.dto.ListenEndResponseDto
import com.nestorian87.eter.data.remote.dto.ListenProgressRequestDto
import com.nestorian87.eter.data.remote.dto.ListenProgressResponseDto
import com.nestorian87.eter.data.remote.dto.ListenStartRequestDto
import com.nestorian87.eter.data.remote.dto.ListenStartResponseDto
import com.nestorian87.eter.data.remote.dto.LoginRequestDto
import com.nestorian87.eter.data.remote.dto.MyPostsResponseDto
import com.nestorian87.eter.data.remote.dto.PostCategoryDto
import com.nestorian87.eter.data.remote.dto.PostCommentDto
import com.nestorian87.eter.data.remote.dto.PostLikeResponseDto
import com.nestorian87.eter.data.remote.dto.PostCommentsResponseDto
import com.nestorian87.eter.data.remote.dto.PostDto
import com.nestorian87.eter.data.remote.dto.PostFeedResponseDto
import com.nestorian87.eter.data.remote.dto.PostListResponseDto
import com.nestorian87.eter.data.remote.dto.PopularPostsResponseDto
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

    @GET("posts/popular")
    suspend fun getPopularPosts(
        @Query("snapshotId") snapshotId: String? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null,
    ): PopularPostsResponseDto

    @GET("posts/search")
    suspend fun searchPosts(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Long? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PostListResponseDto

    @GET("posts/feed")
    suspend fun getSubscriptionFeed(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null,
    ): PostFeedResponseDto

    @GET("posts/liked")
    suspend fun getLikedPosts(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PostListResponseDto

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

    @POST("posts/{postId}/listen/start")
    suspend fun startListen(
        @Path("postId") postId: Long,
        @Body request: ListenStartRequestDto,
    ): ListenStartResponseDto

    @POST("posts/{postId}/listen/progress")
    suspend fun updateListenProgress(
        @Path("postId") postId: Long,
        @Body request: ListenProgressRequestDto,
    ): ListenProgressResponseDto

    @POST("posts/{postId}/listen/end")
    suspend fun endListen(
        @Path("postId") postId: Long,
        @Body request: ListenEndRequestDto,
    ): ListenEndResponseDto

    @GET("posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("sort") sort: String? = null,
    ): PostCommentsResponseDto

    @GET("posts/comments/{commentId}/replies")
    suspend fun getCommentReplies(
        @Path("commentId") commentId: Long,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
        @Query("sort") sort: String? = null,
    ): PostCommentsResponseDto

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Long,
    ): PostLikeResponseDto

    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(
        @Path("postId") postId: Long,
    ): PostLikeResponseDto

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CreateCommentRequestDto,
    ): PostCommentDto

    @POST("posts/comments/{commentId}/like")
    suspend fun likeComment(
        @Path("commentId") commentId: Long,
    ): CommentLikeResponseDto

    @DELETE("posts/comments/{commentId}/like")
    suspend fun unlikeComment(
        @Path("commentId") commentId: Long,
    ): CommentLikeResponseDto

    @DELETE("posts/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Long,
    ): DeletePostResponseDto

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: Long,
    ): DeletePostResponseDto

    @NoAuth
    @POST("auth/logout")
    suspend fun logout()
}
