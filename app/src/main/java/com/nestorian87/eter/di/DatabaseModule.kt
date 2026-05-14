package com.nestorian87.eter.di

import android.content.Context
import androidx.room.Room
import com.nestorian87.eter.data.local.db.EterDatabase
import com.nestorian87.eter.data.local.db.dao.EditPostDraftDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val ETER_DATABASE_NAME = "eter.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEterDatabase(
        @ApplicationContext context: Context,
    ): EterDatabase = Room.databaseBuilder(
        context,
        EterDatabase::class.java,
        ETER_DATABASE_NAME,
    ).build()

    @Provides
    @Singleton
    fun provideEditPostDraftDao(
        database: EterDatabase,
    ): EditPostDraftDao = database.editPostDraftDao()
}
