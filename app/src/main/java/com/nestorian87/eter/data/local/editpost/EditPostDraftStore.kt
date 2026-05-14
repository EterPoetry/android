package com.nestorian87.eter.data.local.editpost

import com.nestorian87.eter.data.mapper.toDomain
import com.nestorian87.eter.data.mapper.toEntity
import com.nestorian87.eter.data.local.db.dao.EditPostDraftDao
import com.nestorian87.eter.domain.model.EditPostDraft
import com.nestorian87.eter.domain.repository.EditPostDraftRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class EditPostDraftStore @Inject constructor(
    private val editPostDraftDao: EditPostDraftDao,
    private val json: Json,
) : EditPostDraftRepository {
    override suspend fun getDraft(postId: Long): EditPostDraft? =
        editPostDraftDao.getByPostId(postId)?.toDomain(json)

    override suspend fun saveDraft(draft: EditPostDraft) {
        editPostDraftDao.upsert(draft.toEntity(json))
    }

    override suspend fun deleteDraft(postId: Long) {
        editPostDraftDao.deleteByPostId(postId)
    }
}
