package com.nestorian87.eter.data.repository

import com.nestorian87.eter.domain.model.Post
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class PostInteractionStore @Inject constructor() {
    private val overridesState = MutableStateFlow<Map<Long, PostInteractionOverride>>(emptyMap())

    val overrides: StateFlow<Map<Long, PostInteractionOverride>> = overridesState.asStateFlow()

    fun apply(post: Post): Post {
        val override = overridesState.value[post.postId] ?: return post
        return post.copy(
            likesCount = override.likesCount ?: post.likesCount,
            isLiked = override.isLiked ?: post.isLiked,
            listens = override.listens ?: post.listens,
        )
    }

    fun isLikePending(postId: Long): Boolean = overridesState.value[postId]?.isLikePending == true

    fun updateLikeOptimistic(post: Post, shouldLike: Boolean) {
        val merged = apply(post)
        val likesCount = if (shouldLike) {
            merged.likesCount + 1
        } else {
            (merged.likesCount - 1).coerceAtLeast(0)
        }
        overridesState.update { current ->
            current + (post.postId to (current[post.postId] ?: PostInteractionOverride()).copy(
                likesCount = likesCount,
                isLiked = shouldLike,
                isLikePending = true,
            ))
        }
    }

    fun resolveLike(postId: Long, likesCount: Int, isLiked: Boolean) {
        overridesState.update { current ->
            current + (postId to (current[postId] ?: PostInteractionOverride()).copy(
                likesCount = likesCount,
                isLiked = isLiked,
                isLikePending = false,
            ))
        }
    }

    fun revertLike(post: Post) {
        overridesState.update { current ->
            current + (post.postId to (current[post.postId] ?: PostInteractionOverride()).copy(
                likesCount = post.likesCount,
                isLiked = post.isLiked,
                isLikePending = false,
            ))
        }
    }

    fun incrementListens(postId: Long) {
        overridesState.update { current ->
            val existing = current[postId]
            current + (postId to (existing ?: PostInteractionOverride()).copy(
                listens = (existing?.listens ?: 0) + 1,
            ))
        }
    }

    fun ensureListenBase(post: Post) {
        overridesState.update { current ->
            val existing = current[post.postId]
            current + (post.postId to (existing ?: PostInteractionOverride()).copy(
                listens = existing?.listens ?: post.listens,
            ))
        }
    }
}
