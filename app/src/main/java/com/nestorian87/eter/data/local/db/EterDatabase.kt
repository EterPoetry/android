package com.nestorian87.eter.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nestorian87.eter.data.local.db.dao.EditPostDraftDao
import com.nestorian87.eter.data.local.db.entity.EditPostDraftEntity

@Database(
    entities = [EditPostDraftEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class EterDatabase : RoomDatabase() {
    abstract fun editPostDraftDao(): EditPostDraftDao
}
