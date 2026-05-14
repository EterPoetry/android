package com.nestorian87.eter.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nestorian87.eter.data.local.db.entity.EditPostDraftEntity

@Dao
interface EditPostDraftDao {

    @Query("SELECT * FROM edit_post_drafts WHERE postId = :postId LIMIT 1")
    suspend fun getByPostId(postId: Long): EditPostDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: EditPostDraftEntity)

    @Query("DELETE FROM edit_post_drafts WHERE postId = :postId")
    suspend fun deleteByPostId(postId: Long)
}
