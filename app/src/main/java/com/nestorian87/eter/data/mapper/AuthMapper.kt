package com.nestorian87.eter.data.mapper

import com.nestorian87.eter.data.remote.dto.AuthResponseDto
import com.nestorian87.eter.data.remote.dto.AuthUserDto
import com.nestorian87.eter.domain.model.AuthSession
import com.nestorian87.eter.domain.model.AuthUser

fun AuthUserDto.toDomain(): AuthUser = AuthUser(
    userId = userId,
    name = name,
    email = email,
    photo = photo,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt,
)

fun AuthResponseDto.toDomain(): AuthSession = AuthSession(
    user = user.toDomain(),
    accessToken = accessToken,
)
