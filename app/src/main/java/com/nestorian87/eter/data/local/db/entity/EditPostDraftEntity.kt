package com.nestorian87.eter.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_post_drafts")
data class EditPostDraftEntity(
    @PrimaryKey
    val postId: Long,
    val title: String,
    val text: String,
    val originAuthorName: String,
    val description: String,
    val selectedCategoriesJson: String,
    val isCopyrightConfirmed: Boolean,
    val updatedAtEpochMs: Long,
)
