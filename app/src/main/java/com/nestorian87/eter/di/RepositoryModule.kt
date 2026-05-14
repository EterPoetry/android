package com.nestorian87.eter.di

import com.nestorian87.eter.data.repository.AuthRepositoryImpl
import com.nestorian87.eter.data.repository.PostRepositoryImpl
import com.nestorian87.eter.data.local.editpost.EditPostDraftStore
import com.nestorian87.eter.data.media.AudioDraftStore
import com.nestorian87.eter.data.media.AudioRecorder
import com.nestorian87.eter.domain.repository.AudioDraftRepository
import com.nestorian87.eter.domain.repository.AudioRecorderRepository
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.domain.repository.EditPostDraftRepository
import com.nestorian87.eter.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl,
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindAudioDraftRepository(
        impl: AudioDraftStore,
    ): AudioDraftRepository

    @Binds
    @Singleton
    abstract fun bindAudioRecorderRepository(
        impl: AudioRecorder,
    ): AudioRecorderRepository

    @Binds
    @Singleton
    abstract fun bindEditPostDraftRepository(
        impl: EditPostDraftStore,
    ): EditPostDraftRepository
}
