package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.EditPostDraft

interface EditPostDraftRepository {
    suspend fun getDraft(postId: Long): EditPostDraft?

    suspend fun saveDraft(draft: EditPostDraft)

    suspend fun deleteDraft(postId: Long)
}
