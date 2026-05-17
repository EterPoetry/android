package com.nestorian87.eter.ui.screens.editpost

import com.nestorian87.eter.domain.model.PostStatus

data class EditPostUiState(
    val postId: Long? = null,
    val postStatus: PostStatus? = null,
    val isLoadingPost: Boolean = true,
    val isSubmitting: Boolean = false,
    val isDeleting: Boolean = false,
    val isReplacingAudio: Boolean = false,
    val isLoadingCategories: Boolean = false,
    val title: String = "",
    val text: String = "",
    val originAuthorName: String = "",
    val description: String = "",
    val audioDisplayName: String = "",
    val audioFileUrl: String? = null,
    val categorySearchQuery: String = "",
    val availableCategories: List<EditPostCategoryUiState> = emptyList(),
    val selectedCategories: List<EditPostCategoryUiState> = emptyList(),
    val isCopyrightConfirmed: Boolean = false,
    val errorMessage: EditPostUiMessage? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
) {
    val isProcessing: Boolean
        get() = postStatus == PostStatus.PROCESSING

    val isEditable: Boolean
        get() = postId != null && postStatus != null && !isProcessing

    val canReplaceAudio: Boolean
        get() = postStatus == PostStatus.DRAFT && !isReplacingAudio && !isSubmitting

    val canDeleteDraft: Boolean
        get() = postStatus == PostStatus.DRAFT && !isReplacingAudio && !isSubmitting && !isDeleting

    val canSave: Boolean
        get() = isEditable &&
            title.isNotBlank() &&
            text.isNotBlank() &&
            isCopyrightConfirmed &&
            !isSubmitting &&
            !isReplacingAudio &&
            !isDeleting

    val selectedCountLabel: String
        get() = "${selectedCategories.size}/${EditPostUiDefaults.CATEGORY_LIMIT}"
}
