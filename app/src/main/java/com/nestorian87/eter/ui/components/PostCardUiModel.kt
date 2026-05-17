package com.nestorian87.eter.ui.components

import com.nestorian87.eter.domain.model.Post

data class PostCardUiModel(
    val post: Post,
    val isLiked: Boolean = false,
)
