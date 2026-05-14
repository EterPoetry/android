package com.nestorian87.eter.data.mapper

import com.nestorian87.eter.data.remote.dto.MyPostsResponseDto
import com.nestorian87.eter.data.remote.dto.PostCategoryDto
import com.nestorian87.eter.data.remote.dto.PostDto
import com.nestorian87.eter.data.remote.dto.PostStatusDto
import com.nestorian87.eter.data.remote.dto.PublicConfigDto
import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostStatus
import com.nestorian87.eter.domain.model.PublicConfig
import com.nestorian87.eter.domain.model.RecordingConfig
import com.nestorian87.eter.domain.model.SubscriptionConfig

fun PublicConfigDto.toDomain(): PublicConfig = PublicConfig(
    recording = RecordingConfig(
        freeDurationLimitMinutes = recording.freeDurationLimitMinutes,
        premiumDurationLimitMinutes = recording.premiumDurationLimitMinutes,
    ),
    subscription = SubscriptionConfig(
        priceUsd = subscription.priceUsd,
    ),
)

fun PostCategoryDto.toDomain(): PostCategory = PostCategory(
    categoryId = categoryId,
    categoryName = categoryName,
    categoryDescription = categoryDescription,
)

fun PostDto.toDomain(): Post = Post(
    postId = postId,
    title = title,
    description = description,
    text = text,
    audioFileName = audioFileName,
    audioFileUrl = audioFileUrl,
    status = status.toDomain(),
    listens = listens,
    originAuthorName = originAuthorName,
    categories = categories.map(PostCategoryDto::toDomain),
    authorId = authorId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MyPostsResponseDto.toDomain(): MyPostsPage = MyPostsPage(
    items = items.map(PostDto::toDomain),
    total = total,
    offset = offset,
)

fun PostStatusDto.toDomain(): PostStatus = when (this) {
    PostStatusDto.draft -> PostStatus.DRAFT
    PostStatusDto.processing -> PostStatus.PROCESSING
    PostStatusDto.published -> PostStatus.PUBLISHED
}
