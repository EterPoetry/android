package com.nestorian87.eter.data.repository

import com.nestorian87.eter.di.qualifier.IoDispatcher
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class PostLikeController @Inject constructor(
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    fun toggleLike(post: Post) {
        val merged = postInteractionStore.apply(post)
        if (postInteractionStore.isLikePending(merged.postId)) {
            return
        }

        val shouldLike = !merged.isLiked
        postInteractionStore.updateLikeOptimistic(merged, shouldLike)
        scope.launch {
            runCatching {
                if (shouldLike) {
                    postRepository.likePost(merged.postId)
                } else {
                    postRepository.unlikePost(merged.postId)
                }
            }.onSuccess { likesCount ->
                postInteractionStore.resolveLike(
                    postId = merged.postId,
                    likesCount = likesCount,
                    isLiked = shouldLike,
                )
            }.onFailure {
                postInteractionStore.revertLike(merged)
            }
        }
    }
}
