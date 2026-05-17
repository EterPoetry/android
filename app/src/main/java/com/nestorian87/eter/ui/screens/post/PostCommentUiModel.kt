package com.nestorian87.eter.ui.screens.post

import com.nestorian87.eter.domain.model.PostComment

data class PostCommentUiModel(
    val comment: PostComment,
    val isLiked: Boolean = comment.isLikedByMe,
)
