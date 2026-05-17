package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.data.remote.dto.CreateCommentRequestDto
import com.nestorian87.eter.data.remote.dto.ListenEndRequestDto
import com.nestorian87.eter.data.remote.dto.ListenProgressRequestDto
import com.nestorian87.eter.data.remote.dto.ListenStartRequestDto
import com.nestorian87.eter.data.remote.dto.UpdatePostRequestDto
import com.nestorian87.eter.domain.model.ListenEndResult
import com.nestorian87.eter.domain.model.ListenProgressResult
import com.nestorian87.eter.domain.model.ListenStartResult
import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.MyPostsQuery
import com.nestorian87.eter.domain.model.MyPostsSortBy
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostComment
import com.nestorian87.eter.domain.model.PostCommentsPage
import com.nestorian87.eter.domain.model.PostCommentsQuery
import com.nestorian87.eter.domain.model.PostCommentsSort
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.PopularPostsPage
import com.nestorian87.eter.domain.model.PublicConfig
import com.nestorian87.eter.domain.model.SortOrder
import com.nestorian87.eter.domain.model.UpdatePostPayload
import com.nestorian87.eter.domain.repository.PostRepository
import java.io.File
import java.net.URLConnection
import javax.inject.Inject
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class PostRepositoryImpl @Inject constructor(
    private val api: Api,
    json: Json,
) : PostRepository {
    private val serverErrorMapper = PostServerErrorMapper(json)

    override suspend fun getPublicConfig(): PublicConfig = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getPublicConfig().toDomain()
    }

    override suspend fun getCategories(search: String?): List<PostCategory> = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getCategories(search = search).map { it.toDomain() }
    }

    override suspend fun createPost(audioFile: File): Post = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCreateOrReplaceAudioException,
    ) {
        api.createPost(
            audio = audioFile.toAudioPart(),
        ).toDomain()
    }

    override suspend fun getPost(postId: Long): Post = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getPost(postId = postId).toDomain()
    }

    override suspend fun getPopularPosts(
        snapshotId: String?,
        cursor: String?,
        limit: Int?,
    ): PopularPostsPage = executePostRequest(
        httpErrorMapper = serverErrorMapper::toPopularPostsException,
    ) {
        api.getPopularPosts(
            snapshotId = snapshotId,
            cursor = cursor,
            limit = limit,
        ).toDomain()
    }

    override suspend fun updatePost(postId: Long, payload: UpdatePostPayload): Post = executePostRequest(
        httpErrorMapper = serverErrorMapper::toUpdatePostException,
    ) {
        api.updatePost(
            postId = postId,
            request = UpdatePostRequestDto(
                title = payload.title,
                description = payload.description,
                text = payload.text,
                originAuthorName = payload.originAuthorName,
                categoryIds = payload.categoryIds,
            ),
        ).toDomain()
    }

    override suspend fun replacePostAudio(postId: Long, audioFile: File): Post = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCreateOrReplaceAudioException,
    ) {
        api.replacePostAudio(
            postId = postId,
            audio = audioFile.toAudioPart(),
        ).toDomain()
    }

    override suspend fun getMyPosts(query: MyPostsQuery): MyPostsPage = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getMyPosts(
            search = query.search,
            sortBy = query.sortBy?.toApiValue(),
            sortOrder = query.sortOrder?.toApiValue(),
            offset = query.offset,
            limit = query.limit,
        ).toDomain()
    }

    override suspend fun startListen(
        postId: Long,
        sessionId: String,
    ): ListenStartResult = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.startListen(
            postId = postId,
            request = ListenStartRequestDto(sessionId = sessionId),
        ).toDomain()
    }

    override suspend fun updateListenProgress(
        postId: Long,
        token: String,
        positionMs: Int,
    ): ListenProgressResult = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.updateListenProgress(
            postId = postId,
            request = ListenProgressRequestDto(
                token = token,
                positionMs = positionMs,
            ),
        ).toDomain()
    }

    override suspend fun endListen(
        postId: Long,
        token: String,
        positionMs: Int,
        sessionId: String?,
    ): ListenEndResult = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.endListen(
            postId = postId,
            request = ListenEndRequestDto(
                token = token,
                positionMs = positionMs,
                sessionId = sessionId,
            ),
        ).toDomain()
    }

    override suspend fun getPostComments(
        postId: Long,
        query: PostCommentsQuery,
    ): PostCommentsPage = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getPostComments(
            postId = postId,
            limit = query.limit,
            cursor = query.cursor,
            sort = query.sort?.toApiValue(),
        ).toDomain()
    }

    override suspend fun getCommentReplies(
        commentId: Long,
        limit: Int?,
        cursor: String?,
    ): PostCommentsPage = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.getCommentReplies(
            commentId = commentId,
            limit = limit,
            cursor = cursor,
            sort = null,
        ).toDomain()
    }

    override suspend fun createComment(
        postId: Long,
        commentText: String,
        replyToCommentId: Long?,
    ): PostComment = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        api.createComment(
            postId = postId,
            request = CreateCommentRequestDto(
                commentText = commentText,
                replyToCommentId = replyToCommentId,
            ),
        ).toDomain()
    }

    override suspend fun likePost(postId: Long): Int = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        val response = api.likePost(postId = postId)
        if (!response.ok) {
            throw PostException(reasons = setOf(PostException.Reason.UNKNOWN))
        }
        response.likesCount
    }

    override suspend fun unlikePost(postId: Long): Int = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        val response = api.unlikePost(postId = postId)
        if (!response.ok) {
            throw PostException(reasons = setOf(PostException.Reason.UNKNOWN))
        }
        response.likesCount
    }

    override suspend fun likeComment(commentId: Long): Int = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        val response = api.likeComment(commentId = commentId)
        if (!response.ok) {
            throw PostException(
                reasons = setOf(PostException.Reason.UNKNOWN),
            )
        }
        response.likesCount
    }

    override suspend fun unlikeComment(commentId: Long): Int = executePostRequest(
        httpErrorMapper = serverErrorMapper::toCommonException,
    ) {
        val response = api.unlikeComment(commentId = commentId)
        if (!response.ok) {
            throw PostException(
                reasons = setOf(PostException.Reason.UNKNOWN),
            )
        }
        response.likesCount
    }

    override suspend fun deleteComment(commentId: Long) {
        executePostRequest(
            httpErrorMapper = serverErrorMapper::toCommonException,
        ) {
            val response = api.deleteComment(commentId = commentId)
            if (!response.ok) {
                throw PostException(
                    reasons = setOf(PostException.Reason.UNKNOWN),
                )
            }
        }
    }

    override suspend fun deletePost(postId: Long) {
        executePostRequest(
            httpErrorMapper = serverErrorMapper::toCommonException,
        ) {
            val response = api.deletePost(postId = postId)
            if (!response.ok) {
                throw PostException(
                    reasons = setOf(PostException.Reason.UNKNOWN),
                )
            }
        }
    }

    private fun File.toAudioPart(): MultipartBody.Part {
        val mediaType = URLConnection.guessContentTypeFromName(name)
            ?.toMediaType()
            ?: DEFAULT_AUDIO_MEDIA_TYPE

        return MultipartBody.Part.createFormData(
            name = AUDIO_PART_NAME,
            filename = name,
            body = asRequestBody(mediaType),
        )
    }

    private fun MyPostsSortBy.toApiValue(): String = when (this) {
        MyPostsSortBy.CREATED_AT -> "createdAt"
        MyPostsSortBy.UPDATED_AT -> "updatedAt"
        MyPostsSortBy.TITLE -> "title"
        MyPostsSortBy.LISTENS -> "listens"
    }

    private fun SortOrder.toApiValue(): String = when (this) {
        SortOrder.ASC -> "asc"
        SortOrder.DESC -> "desc"
    }

    private fun PostCommentsSort.toApiValue(): String = when (this) {
        PostCommentsSort.NEWEST -> "newest"
        PostCommentsSort.OLDEST -> "oldest"
        PostCommentsSort.POPULAR -> "popular"
    }

    private companion object {
        const val AUDIO_PART_NAME = "audio"
        val DEFAULT_AUDIO_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
