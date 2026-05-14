package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.remote.api.Api
import com.nestorian87.eter.data.remote.dto.UpdatePostRequestDto
import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.MyPostsQuery
import com.nestorian87.eter.domain.model.MyPostsSortBy
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostException
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

    private companion object {
        const val AUDIO_PART_NAME = "audio"
        val DEFAULT_AUDIO_MEDIA_TYPE = "application/octet-stream".toMediaType()
    }
}
