package com.nestorian87.eter.data.local.datastore

import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostAuthor
import com.nestorian87.eter.domain.model.PostCategory
import com.nestorian87.eter.domain.model.PostStatus
import com.nestorian87.eter.domain.model.PostTextSynchronization

fun Post.toPlayerSnapshot(
    currentTimeSeconds: Float,
    volume: Float,
    isMuted: Boolean,
): PostPlayerSnapshot = PostPlayerSnapshot(
    post = PersistedPostSnapshot(
        postId = postId,
        title = title,
        description = description,
        text = text,
        audioFileName = audioFileName,
        audioFileUrl = audioFileUrl,
        audioDurationSeconds = audioDurationSeconds,
        status = status.name,
        listens = listens,
        likesCount = likesCount,
        isLiked = isLiked,
        commentsCount = commentsCount,
        originAuthorName = originAuthorName,
        textSynchronization = textSynchronization.map { sync ->
            PersistedPostTextSynchronization(
                lineIndex = sync.lineIndex,
                audioStartMomentMs = sync.audioStartMomentMs,
            )
        },
        categories = categories.map { category ->
            PersistedPostCategory(
                categoryId = category.categoryId,
                categoryName = category.categoryName,
            )
        },
        authorId = authorId,
        author = author?.let { authorSnapshot ->
            PersistedPostAuthor(
                userId = authorSnapshot.userId,
                name = authorSnapshot.name,
                username = authorSnapshot.username,
                photo = authorSnapshot.photo,
                isPremium = authorSnapshot.isPremium,
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
    ),
    currentTimeSeconds = currentTimeSeconds,
    volume = volume,
    isMuted = isMuted,
)

fun PersistedPostSnapshot.toDomain(): Post = Post(
    postId = postId,
    title = title,
    description = description,
    text = text,
    audioFileName = audioFileName,
    audioFileUrl = audioFileUrl,
    audioDurationSeconds = audioDurationSeconds,
    status = PostStatus.valueOf(status),
    listens = listens,
    likesCount = likesCount,
    isLiked = isLiked,
    commentsCount = commentsCount,
    originAuthorName = originAuthorName,
    textSynchronization = textSynchronization.map { sync ->
        PostTextSynchronization(
            lineIndex = sync.lineIndex,
            audioStartMomentMs = sync.audioStartMomentMs,
        )
    },
    categories = categories.map { category ->
        PostCategory(
            categoryId = category.categoryId,
            categoryName = category.categoryName,
        )
    },
    authorId = authorId,
    author = author?.let { authorSnapshot ->
        PostAuthor(
            userId = authorSnapshot.userId,
            name = authorSnapshot.name,
            username = authorSnapshot.username,
            photo = authorSnapshot.photo,
            isPremium = authorSnapshot.isPremium,
        )
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
)
