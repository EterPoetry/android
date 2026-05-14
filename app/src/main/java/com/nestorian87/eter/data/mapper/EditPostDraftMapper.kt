package com.nestorian87.eter.data.mapper

import com.nestorian87.eter.data.local.db.entity.EditPostDraftEntity
import com.nestorian87.eter.domain.model.EditPostDraft
import com.nestorian87.eter.domain.model.EditPostDraftCategory
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun EditPostDraftEntity.toDomain(json: Json): EditPostDraft = EditPostDraft(
    postId = postId,
    title = title,
    text = text,
    originAuthorName = originAuthorName,
    description = description,
    selectedCategories = runCatching {
        json.decodeFromString(
            ListSerializer(EditPostDraftCategory.serializer()),
            selectedCategoriesJson,
        )
    }.getOrDefault(emptyList()),
    isCopyrightConfirmed = isCopyrightConfirmed,
    updatedAtEpochMs = updatedAtEpochMs,
)

fun EditPostDraft.toEntity(json: Json): EditPostDraftEntity = EditPostDraftEntity(
    postId = postId,
    title = title,
    text = text,
    originAuthorName = originAuthorName,
    description = description,
    selectedCategoriesJson = json.encodeToString(
        ListSerializer(EditPostDraftCategory.serializer()),
        selectedCategories,
    ),
    isCopyrightConfirmed = isCopyrightConfirmed,
    updatedAtEpochMs = updatedAtEpochMs,
)
