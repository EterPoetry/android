package com.nestorian87.eter.ui.screens.post

import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostCommentsSort

data class PostUiState(
    val isLoadingPost: Boolean = true,
    val post: Post? = null,
    val isPostLiked: Boolean = false,
    val pendingPostLike: Boolean = false,
    val postError: Throwable? = null,
    val comments: List<PostCommentUiModel> = emptyList(),
    val commentsSort: PostCommentsSort = PostCommentsSort.NEWEST,
    val commentsNextCursor: String? = null,
    val isLoadingComments: Boolean = false,
    val isLoadingMoreComments: Boolean = false,
    val commentsError: Throwable? = null,
    val pendingCommentLikeIds: Set<Long> = emptySet(),
    val pendingDeleteCommentIds: Set<Long> = emptySet(),
    val expandedRepliesCommentIds: Set<Long> = emptySet(),
    val loadingRepliesCommentIds: Set<Long> = emptySet(),
    val loadingMoreRepliesCommentIds: Set<Long> = emptySet(),
    val repliesByCommentId: Map<Long, List<PostCommentUiModel>> = emptyMap(),
    val repliesNextCursorByCommentId: Map<Long, String?> = emptyMap(),
    val composerText: String = "",
    val isSubmittingComment: Boolean = false,
    val openReplyComposerCommentId: Long? = null,
    val replyDraftByCommentId: Map<Long, String> = emptyMap(),
    val currentUserId: Long? = null,
    val isCurrentPostActive: Boolean = false,
    val isCurrentPostPlaying: Boolean = false,
    val currentPlaybackTimeMs: Long = 0L,
)
