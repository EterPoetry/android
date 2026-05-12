package com.nestorian87.eter.data.repository

import com.nestorian87.eter.data.remote.dto.ApiErrorResponseDto
import com.nestorian87.eter.data.remote.dto.ApiFieldErrorDto
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.model.FieldViolation
import com.nestorian87.eter.domain.model.ServerValidationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

internal class AuthServerErrorMapper(
    private val json: Json,
) {
    fun toRegistrationException(error: HttpException): Throwable {
        val fieldViolations = error
            .parseErrorBody()
            ?.errors
            .orEmpty()
            .mapNotNull(::toFieldViolation)
            .distinct()
            .toSet()

        if (error.code() == 400 || error.code() == 409) {
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

        return AuthException(
            reasons = setOf(AuthException.Reason.UNKNOWN),
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
}
