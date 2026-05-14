package com.nestorian87.eter.domain.model

data class EditPostDraft(
    val postId: Long,
    val title: String,
    val text: String,
    val originAuthorName: String,
    val description: String,
    val selectedCategories: List<EditPostDraftCategory>,
    val isCopyrightConfirmed: Boolean,
    val updatedAtEpochMs: Long,
)
