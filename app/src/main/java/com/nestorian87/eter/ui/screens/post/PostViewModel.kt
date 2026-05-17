package com.nestorian87.eter.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.data.repository.PostInteractionStore
import com.nestorian87.eter.data.repository.PostLikeController
import com.nestorian87.eter.domain.model.PostCommentsQuery
import com.nestorian87.eter.domain.model.PostCommentsSort
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.domain.repository.PostRepository
import com.nestorian87.eter.service.PostPlayerManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PostViewModel.Factory::class)
class PostViewModel @AssistedInject constructor(
    @Assisted private val postId: Long,
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val postInteractionStore: PostInteractionStore,
    private val postLikeController: PostLikeController,
    private val postPlayerManager: PostPlayerManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()
    private var sourcePost: com.nestorian87.eter.domain.model.Post? = null

    init {
        observeSession()
        observePostInteractions()
        observePlayerState()
        loadPost()
        loadComments(reset = true)
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                _uiState.update {
                    it.copy(currentUserId = session?.user?.userId)
                }
            }
        }
    }

    fun retryLoadPost() {
        loadPost()
    }

    fun loadMoreComments() {
        val state = _uiState.value
        if (
            state.isLoadingComments ||
            state.isLoadingMoreComments ||
            state.commentsNextCursor == null
        ) {
            return
        }
        loadComments(reset = false)
    }

    fun onSortChanged(sort: PostCommentsSort) {
        if (_uiState.value.commentsSort == sort) {
            return
        }

        _uiState.update {
            it.copy(
                commentsSort = sort,
                comments = emptyList(),
                commentsNextCursor = null,
                commentsError = null,
            )
        }
        loadComments(reset = true)
    }

    fun onTogglePostLike() {
        _uiState.value.post?.let(postLikeController::toggleLike)
    }

    fun onPlayPost() {
        val currentPost = _uiState.value.post ?: return
        viewModelScope.launch {
            val resolvedPost = runCatching {
                postRepository.getPost(postId = postId)
            }.getOrElse { currentPost }
            sourcePost = resolvedPost
            postInteractionStore.ensureListenBase(resolvedPost)
            publishPostUi(
                isLoadingPost = false,
                postError = null,
            )
            postPlayerManager.updatePostMetadata(resolvedPost)
            postPlayerManager.togglePostPlayback(resolvedPost)
        }
    }

    fun onSeekAndPlay(audioStartMomentMs: Int) {
        val post = _uiState.value.post ?: return
        postPlayerManager.playPost(
            post = post,
            startPositionMs = audioStartMomentMs.toLong(),
        )
    }

    fun onToggleCommentLike(commentId: Long, parentCommentId: Long?) {
        val state = _uiState.value
        if (state.pendingCommentLikeIds.contains(commentId)) {
            return
        }

        val currentItem = state.findCommentById(
            commentId = commentId,
            parentCommentId = parentCommentId,
        ) ?: return
        val shouldLike = !currentItem.isLiked

        _uiState.update {
            it.copy(
                pendingCommentLikeIds = it.pendingCommentLikeIds + commentId,
                comments = it.comments.updateCommentItem(commentId, parentCommentId) { item ->
                    item.toggleLike()
                },
                repliesByCommentId = it.repliesByCommentId.updateReplyItems(commentId, parentCommentId) { item ->
                    item.toggleLike()
                },
            )
        }

        viewModelScope.launch {
            runCatching {
                if (shouldLike) {
                    postRepository.likeComment(commentId = commentId)
                } else {
                    postRepository.unlikeComment(commentId = commentId)
                }
            }.onSuccess { likesCount ->
                _uiState.update { current ->
                    current.copy(
                        pendingCommentLikeIds = current.pendingCommentLikeIds - commentId,
                        comments = current.comments.updateCommentItem(commentId, parentCommentId) { item ->
                            item.copy(comment = item.comment.copy(likesCount = likesCount))
                        },
                        repliesByCommentId = current.repliesByCommentId.updateReplyItems(commentId, parentCommentId) { item ->
                            item.copy(comment = item.comment.copy(likesCount = likesCount))
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        pendingCommentLikeIds = current.pendingCommentLikeIds - commentId,
                        commentsError = error,
                        comments = current.comments.updateCommentItem(commentId, parentCommentId) { item ->
                            item.toggleLike()
                        },
                        repliesByCommentId = current.repliesByCommentId.updateReplyItems(commentId, parentCommentId) { item ->
                            item.toggleLike()
                        },
                    )
                }
            }
        }
    }

    fun onDeleteComment(commentId: Long, parentCommentId: Long?) {
        if (_uiState.value.pendingDeleteCommentIds.contains(commentId)) {
            return
        }

        _uiState.update {
            it.copy(
                pendingDeleteCommentIds = it.pendingDeleteCommentIds + commentId,
                commentsError = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                postRepository.deleteComment(commentId = commentId)
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        pendingDeleteCommentIds = current.pendingDeleteCommentIds - commentId,
                        post = current.post?.copy(
                            commentsCount = (current.post.commentsCount - 1).coerceAtLeast(0),
                        ),
                        comments = if (parentCommentId == null) {
                            current.comments.filterNot { item ->
                                item.comment.commentId == commentId
                            }
                        } else {
                            current.comments.updateCommentItem(parentCommentId, null) { parent ->
                                parent.copy(
                                    comment = parent.comment.copy(
                                        repliesCount = (parent.comment.repliesCount - 1).coerceAtLeast(0),
                                    ),
                                )
                            }
                        },
                        repliesByCommentId = current.repliesByCommentId
                            .let { repliesByParent ->
                                if (parentCommentId == null) {
                                    repliesByParent - commentId
                                } else {
                                    repliesByParent.mapValues { (key, value) ->
                                        if (key != parentCommentId) {
                                            value
                                        } else {
                                            value.filterNot { reply -> reply.comment.commentId == commentId }
                                        }
                                    }
                                }
                            },
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        pendingDeleteCommentIds = current.pendingDeleteCommentIds - commentId,
                        commentsError = error,
                    )
                }
            }
        }
    }

    fun onCommentComposerChanged(value: String) {
        _uiState.update {
            it.copy(
                composerText = value.take(MAX_COMMENT_LENGTH),
            )
        }
    }

    fun onSendComment() {
        val text = _uiState.value.composerText.trim()
        if (text.isBlank()) {
            return
        }

        _uiState.update {
            it.copy(
                isSubmittingComment = true,
                commentsError = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                postRepository.createComment(
                    postId = postId,
                    commentText = text,
                )
            }.onSuccess { createdComment ->
                _uiState.update { current ->
                    current.copy(
                        isSubmittingComment = false,
                        composerText = "",
                        comments = listOf(PostCommentUiModel(comment = createdComment)) + current.comments,
                        post = current.post?.copy(commentsCount = current.post.commentsCount + 1),
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isSubmittingComment = false,
                        commentsError = error,
                    )
                }
            }
        }
    }

    fun onReplyComposerOpen(commentId: Long) {
        _uiState.update {
            it.copy(openReplyComposerCommentId = commentId)
        }
    }

    fun onReplyComposerDismiss() {
        _uiState.update {
            it.copy(openReplyComposerCommentId = null)
        }
    }

    fun onReplyDraftChanged(commentId: Long, value: String) {
        _uiState.update {
            it.copy(
                replyDraftByCommentId = it.replyDraftByCommentId + (commentId to value.take(MAX_COMMENT_LENGTH)),
            )
        }
    }

    fun onSendReply(commentId: Long) {
        val state = _uiState.value
        val parentComment = state.comments.firstOrNull { item -> item.comment.commentId == commentId } ?: return
        val draft = state.replyDraftByCommentId[commentId].orEmpty().trim()
        if (draft.isBlank()) {
            return
        }

        viewModelScope.launch {
            runCatching {
                postRepository.createComment(
                    postId = postId,
                    commentText = draft,
                    replyToCommentId = commentId,
                )
            }.onSuccess { createdReply ->
                _uiState.update { current ->
                    current.copy(
                        openReplyComposerCommentId = null,
                        replyDraftByCommentId = current.replyDraftByCommentId + (commentId to ""),
                        expandedRepliesCommentIds = current.expandedRepliesCommentIds + commentId,
                        repliesByCommentId = current.repliesByCommentId + (
                            commentId to (current.repliesByCommentId[commentId].orEmpty() + listOf(PostCommentUiModel(comment = createdReply)))
                            ),
                        comments = current.comments.map { item ->
                            if (item.comment.commentId == parentComment.comment.commentId) {
                                item.copy(comment = item.comment.copy(repliesCount = item.comment.repliesCount + 1))
                            } else {
                                item
                            }
                        },
                        post = current.post?.copy(commentsCount = current.post.commentsCount + 1),
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        commentsError = error,
                    )
                }
            }
        }
    }

    fun onToggleReplies(commentId: Long) {
        val state = _uiState.value
        if (state.expandedRepliesCommentIds.contains(commentId)) {
            _uiState.update {
                it.copy(expandedRepliesCommentIds = it.expandedRepliesCommentIds - commentId)
            }
            return
        }

        _uiState.update {
            it.copy(expandedRepliesCommentIds = it.expandedRepliesCommentIds + commentId)
        }

        if (state.repliesByCommentId[commentId].isNullOrEmpty()) {
            loadReplies(commentId = commentId, loadMore = false)
        }
    }

    fun onLoadMoreReplies(commentId: Long) {
        val state = _uiState.value
        if (state.repliesNextCursorByCommentId[commentId] == null) {
            return
        }
        if (state.loadingRepliesCommentIds.contains(commentId) || state.loadingMoreRepliesCommentIds.contains(commentId)) {
            return
        }
        loadReplies(commentId = commentId, loadMore = true)
    }

    private fun loadPost() {
        _uiState.update {
            it.copy(
                isLoadingPost = true,
                postError = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                postRepository.getPost(postId = postId)
            }.onSuccess { post ->
                sourcePost = post
                postInteractionStore.ensureListenBase(post)
                postPlayerManager.updatePostMetadata(post)
                publishPostUi(isLoadingPost = false, postError = null)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingPost = false,
                        postError = error,
                    )
                }
            }
        }
    }

    private fun observePostInteractions() {
        viewModelScope.launch {
            postInteractionStore.overrides.collect {
                publishPostUi()
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            postPlayerManager.uiState.collect { playerState ->
                _uiState.update { current ->
                    current.copy(
                        isCurrentPostActive = playerState.activePost?.postId == postId,
                        isCurrentPostPlaying = playerState.isPlaying && playerState.activePost?.postId == postId,
                        currentPlaybackTimeMs = if (playerState.activePost?.postId == postId) {
                            (playerState.currentTimeSeconds * 1000f).toLong()
                        } else {
                            0L
                        },
                    )
                }
            }
        }
    }

    private fun publishPostUi(
        isLoadingPost: Boolean = _uiState.value.isLoadingPost,
        postError: Throwable? = _uiState.value.postError,
    ) {
        val mergedPost = sourcePost?.let(postInteractionStore::apply)
        val isPendingLike = mergedPost?.let { postInteractionStore.isLikePending(it.postId) } ?: false
        _uiState.update { current ->
            current.copy(
                isLoadingPost = isLoadingPost,
                post = mergedPost,
                isPostLiked = mergedPost?.isLiked ?: false,
                pendingPostLike = isPendingLike,
                postError = postError,
            )
        }
    }

    private fun loadComments(reset: Boolean) {
        _uiState.update {
            it.copy(
                isLoadingComments = reset,
                isLoadingMoreComments = !reset,
                commentsError = null,
            )
        }

        viewModelScope.launch {
            val cursor = if (reset) {
                null
            } else {
                _uiState.value.commentsNextCursor
            }

            runCatching {
                postRepository.getPostComments(
                    postId = postId,
                    query = PostCommentsQuery(
                        limit = COMMENTS_PAGE_LIMIT,
                        cursor = cursor,
                        sort = _uiState.value.commentsSort,
                    ),
                )
            }.onSuccess { page ->
                _uiState.update { current ->
                    current.copy(
                        comments = if (reset) {
                            page.items.map(::PostCommentUiModel)
                        } else {
                            current.comments + page.items.map(::PostCommentUiModel)
                        },
                        commentsNextCursor = page.nextCursor,
                        isLoadingComments = false,
                        isLoadingMoreComments = false,
                        commentsError = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isLoadingComments = false,
                        isLoadingMoreComments = false,
                        commentsError = error,
                    )
                }
            }
        }
    }

    private fun loadReplies(commentId: Long, loadMore: Boolean) {
        _uiState.update {
            if (loadMore) {
                it.copy(
                    loadingMoreRepliesCommentIds = it.loadingMoreRepliesCommentIds + commentId,
                )
            } else {
                it.copy(
                    loadingRepliesCommentIds = it.loadingRepliesCommentIds + commentId,
                )
            }
        }

        viewModelScope.launch {
            val cursor = if (loadMore) {
                _uiState.value.repliesNextCursorByCommentId[commentId]
            } else {
                null
            }

            runCatching {
                postRepository.getCommentReplies(
                    commentId = commentId,
                    limit = COMMENTS_PAGE_LIMIT,
                    cursor = cursor,
                )
            }.onSuccess { page ->
                _uiState.update { current ->
                    current.copy(
                        repliesByCommentId = current.repliesByCommentId + (
                            commentId to if (loadMore) {
                                current.repliesByCommentId[commentId].orEmpty() + page.items.map(::PostCommentUiModel)
                            } else {
                                page.items.map(::PostCommentUiModel)
                            }
                            ),
                        repliesNextCursorByCommentId = current.repliesNextCursorByCommentId + (
                            commentId to page.nextCursor
                            ),
                        loadingRepliesCommentIds = current.loadingRepliesCommentIds - commentId,
                        loadingMoreRepliesCommentIds = current.loadingMoreRepliesCommentIds - commentId,
                    )
                }
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        loadingRepliesCommentIds = current.loadingRepliesCommentIds - commentId,
                        loadingMoreRepliesCommentIds = current.loadingMoreRepliesCommentIds - commentId,
                    )
                }
            }
        }
    }

    private fun PostCommentUiModel.toggleLike(): PostCommentUiModel {
        val shouldLike = !isLiked
        return copy(
            isLiked = shouldLike,
            comment = comment.copy(
                likesCount = if (shouldLike) {
                    comment.likesCount + 1
                } else {
                    (comment.likesCount - 1).coerceAtLeast(0)
                },
            ),
        )
    }

    private fun PostUiState.findCommentById(
        commentId: Long,
        parentCommentId: Long?,
    ): PostCommentUiModel? = if (parentCommentId == null) {
        comments.firstOrNull { item -> item.comment.commentId == commentId }
    } else {
        repliesByCommentId[parentCommentId]
            .orEmpty()
            .firstOrNull { item -> item.comment.commentId == commentId }
    }

    private fun List<PostCommentUiModel>.updateCommentItem(
        commentId: Long,
        parentCommentId: Long?,
        transform: (PostCommentUiModel) -> PostCommentUiModel,
    ): List<PostCommentUiModel> {
        if (parentCommentId != null) {
            return this
        }
        return map { item ->
            if (item.comment.commentId == commentId) {
                transform(item)
            } else {
                item
            }
        }
    }

    private fun Map<Long, List<PostCommentUiModel>>.updateReplyItems(
        commentId: Long,
        parentCommentId: Long?,
        transform: (PostCommentUiModel) -> PostCommentUiModel,
    ): Map<Long, List<PostCommentUiModel>> {
        if (parentCommentId == null) {
            return this
        }
        return mapValues { (key, value) ->
            if (key != parentCommentId) {
                value
            } else {
                value.map { item ->
                    if (item.comment.commentId == commentId) {
                        transform(item)
                    } else {
                        item
                    }
                }
            }
        }
    }

    private companion object {
        const val COMMENTS_PAGE_LIMIT = 20
        const val MAX_COMMENT_LENGTH = 5000
    }

    @AssistedFactory
    interface Factory {
        fun create(postId: Long): PostViewModel
    }
}
