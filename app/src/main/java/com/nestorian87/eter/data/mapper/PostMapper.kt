package com.nestorian87.eter.data.mapper

import com.nestorian87.eter.data.remote.dto.ListenEndResponseDto
import com.nestorian87.eter.data.remote.dto.ListenProgressResponseDto
import com.nestorian87.eter.data.remote.dto.ListenStartResponseDto
import com.nestorian87.eter.data.remote.dto.MyPostsResponseDto
import com.nestorian87.eter.data.remote.dto.PopularPostsResponseDto
import com.nestorian87.eter.data.remote.dto.PostAuthorDto
import com.nestorian87.eter.data.remote.dto.PostCategoryDto
import com.nestorian87.eter.data.remote.dto.PostCommentDto
import com.nestorian87.eter.data.remote.dto.PostCommentsResponseDto
import com.nestorian87.eter.data.remote.dto.PostDto
import com.nestorian87.eter.data.remote.dto.PostStatusDto
import com.nestorian87.eter.data.remote.dto.PostTextSynchronizationDto
import com.nestorian87.eter.data.remote.dto.PublicConfigDto
import com.nestorian87.eter.domain.model.ListenEndResult
import com.nestorian87.eter.domain.model.ListenProgressResult
import com.nestorian87.eter.domain.model.ListenStartResult
import com.nestorian87.eter.domain.model.MyPostsPage
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostAuthor
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostComment
import com.nestorian87.eter.domain.model.PostCommentsPage
import com.nestorian87.eter.domain.model.PostStatus
import com.nestorian87.eter.domain.model.PostTextSynchronization
import com.nestorian87.eter.domain.model.PopularPostsPage
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
    audioDurationSeconds = audioDurationSeconds,
    status = status.toDomain(),
    listens = listens,
    likesCount = likesCount,
    isLiked = isLiked,
    commentsCount = commentsCount,
    originAuthorName = originAuthorName,
    textSynchronization = textSynchronization
        .sortedBy(PostTextSynchronizationDto::lineIndex)
        .map(PostTextSynchronizationDto::toDomain),
    categories = categories.map(PostCategoryDto::toDomain),
    authorId = authorId,
    author = author?.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun MyPostsResponseDto.toDomain(): MyPostsPage = MyPostsPage(
    items = items.map(PostDto::toDomain),
    total = total,
    offset = offset,
)

fun PopularPostsResponseDto.toDomain(): PopularPostsPage = PopularPostsPage(
    items = items.map(PostDto::toDomain),
    total = total,
    snapshotId = snapshotId,
    snapshotGeneratedAt = snapshotGeneratedAt,
    nextCursor = nextCursor,
    hasMore = hasMore,
)

fun ListenStartResponseDto.toDomain(): ListenStartResult = ListenStartResult(
    token = token,
    listenedMs = listenedMs,
    trackDurationMs = trackDurationMs,
    isSuspicious = isSuspicious,
)

fun ListenProgressResponseDto.toDomain(): ListenProgressResult = ListenProgressResult(
    listenedMs = listenedMs,
    isSuspicious = isSuspicious,
    suspiciousReason = suspiciousReason,
)

fun ListenEndResponseDto.toDomain(): ListenEndResult = ListenEndResult(
    listenedMs = listenedMs,
    isSuspicious = isSuspicious,
    suspiciousReason = suspiciousReason,
    counted = counted,
    countedAt = countedAt,
    thresholdReached = thresholdReached,
)

fun PostAuthorDto.toDomain(): PostAuthor = PostAuthor(
    userId = userId,
    name = name,
    username = username,
    photo = photo,
    isPremium = isPremium,
)

fun PostTextSynchronizationDto.toDomain(): PostTextSynchronization = PostTextSynchronization(
    lineIndex = lineIndex,
    audioStartMomentMs = audioStartMomentMs,
)

fun PostCommentDto.toDomain(): PostComment = PostComment(
    commentId = commentId,
    postId = postId,
    parentCommentId = parentCommentId,
    authorId = authorId,
    author = author?.toDomain(),
    text = text,
    likesCount = likesCount,
    repliesCount = repliesCount,
    isLikedByMe = isLikedByMe,
    isLikedByAuthor = isLikedByAuthor,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PostCommentsResponseDto.toDomain(): PostCommentsPage = PostCommentsPage(
    items = items.map(PostCommentDto::toDomain),
    nextCursor = nextCursor,
)

fun PostStatusDto.toDomain(): PostStatus = when (this) {
    PostStatusDto.draft -> PostStatus.DRAFT
    PostStatusDto.processing -> PostStatus.PROCESSING
    PostStatusDto.published -> PostStatus.PUBLISHED
}
