package com.nestorian87.eter.ui.screens.editpost

data class EditPostCategoryUiState(
    val categoryId: Long,
    val categoryName: String,
)

object EditPostUiDefaults {
    const val CATEGORY_LIMIT = 3
}
