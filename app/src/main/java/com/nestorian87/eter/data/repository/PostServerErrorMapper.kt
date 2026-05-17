package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.remote.dto.ApiErrorResponseDto
import com.nestorian87.eter.data.remote.dto.ApiFieldErrorDto
import com.nestorian87.eter.domain.model.FieldViolation
import com.nestorian87.eter.domain.model.PostException
import com.nestorian87.eter.domain.model.ServerValidationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

internal class PostServerErrorMapper(
    private val json: Json,
) {
    fun toCreateOrReplaceAudioException(error: HttpException): Throwable {
        val errorBody = error.parseErrorBody()
        val serverMessage = errorBody.message()

        if (
            error.code() == 400 && (
                errorBody.hasErrorCode(AUDIO_DURATION_EXCEEDED_CODE) ||
                    serverMessage?.startsWith(AUDIO_DURATION_EXCEEDED_PREFIX) == true
                )
        ) {
            return PostException(
                reasons = setOf(PostException.Reason.AUDIO_DURATION_LIMIT_EXCEEDED),
                cause = error,
            )
        }

        if (error.code() == 403) {
            return PostException(
                reasons = setOf(PostException.Reason.AUDIO_REPLACEMENT_NOT_ALLOWED),
                cause = error,
            )
        }

        return toCommonException(error)
    }

    fun toUpdatePostException(error: HttpException): Throwable {
        val errorBody = error.parseErrorBody()
        val serverMessage = errorBody.message()

        if (
            error.code() == 403 && (
                errorBody.hasErrorCode(POST_IS_STILL_PROCESSING_CODE) ||
                    serverMessage == POST_IS_STILL_PROCESSING_MESSAGE
                )
        ) {
            return PostException(
                reasons = setOf(PostException.Reason.POST_IS_STILL_PROCESSING),
                cause = error,
            )
        }

        return toCommonException(error)
    }

    fun toPopularPostsException(error: HttpException): Throwable {
        val errorBody = error.parseErrorBody()

        if (
            error.code() == 409 &&
            errorBody.hasErrorCode(POPULAR_SNAPSHOT_EXPIRED_CODE)
        ) {
            return PostException(
                reasons = setOf(PostException.Reason.POPULAR_SNAPSHOT_EXPIRED),
                cause = error,
            )
        }

        return toCommonException(error)
    }

    fun toCommonException(error: HttpException): Throwable {
        val errorBody = error.parseErrorBody()
        val fieldViolations = errorBody
            ?.errors
            .orEmpty()
            .mapNotNull(::toFieldViolation)
            .distinct()
            .toSet()

        if ((error.code() == 400 || error.code() == 409) && fieldViolations.isNotEmpty()) {
            return ServerValidationException(
                reason = if (error.code() == 400) {
                    ServerValidationException.Reason.INVALID_DATA
                } else {
                    ServerValidationException.Reason.CONFLICT
                },
                fieldViolations = fieldViolations,
                cause = error,
            )
        }

        if (error.code() == 404) {
            return PostException(
                reasons = setOf(PostException.Reason.NOT_FOUND),
                cause = error,
            )
        }

        if (error.code() == 403) {
            return PostException(
                reasons = setOf(PostException.Reason.FORBIDDEN),
                cause = error,
            )
        }

        if (error.code() == 401) {
            return PostException(
                reasons = setOf(PostException.Reason.FORBIDDEN),
                cause = error,
            )
        }

        return PostException(
            reasons = setOf(PostException.Reason.UNKNOWN),
            cause = error,
        )
    }

    private fun HttpException.parseErrorBody(): ApiErrorResponseDto? {
        val rawBody = response()?.errorBody()?.string().orEmpty()
        if (rawBody.isBlank()) {
            return null
        }

        return runCatching {
            json.decodeFromString<ApiErrorResponseDto>(rawBody)
        }.getOrNull()
    }

    private fun ApiErrorResponseDto?.message(): String? = this?.message?.trim()?.takeIf {
        it.isNotEmpty()
    }

    private fun ApiErrorResponseDto?.hasErrorCode(expectedCode: String): Boolean {
        val normalizedExpectedCode = expectedCode.normalizeErrorCode()
        if (this == null) {
            return false
        }

        if (code.normalizeErrorCode() == normalizedExpectedCode) {
            return true
        }

        return errors.any { error ->
            error.code.normalizeErrorCode() == normalizedExpectedCode
        }
    }

    private fun String?.normalizeErrorCode(): String = this
        ?.trim()
        ?.replace(Regex("[^A-Za-z0-9]+"), "_")
        ?.uppercase()
        .orEmpty()

    private fun toFieldViolation(
        error: ApiFieldErrorDto,
    ): FieldViolation? {
        val field = error.field?.trim().orEmpty()
        val code = error.code?.trim().orEmpty()
        val message = error.message?.trim()?.takeIf { it.isNotEmpty() }

        if (field.isBlank() || code.isBlank()) {
            return null
        }

        return FieldViolation(
            field = field,
            code = code,
            message = message,
        )
    }

    private companion object {
        const val AUDIO_DURATION_EXCEEDED_CODE = "audio_duration_exceeded"
        const val AUDIO_DURATION_EXCEEDED_PREFIX = "Audio duration exceeds"
        const val POST_IS_STILL_PROCESSING_CODE = "post_is_still_processing"
        const val POPULAR_SNAPSHOT_EXPIRED_CODE = "popular_snapshot_expired"
        const val POST_IS_STILL_PROCESSING_MESSAGE =
            "Post is still processing and cannot be edited."
    }
}
