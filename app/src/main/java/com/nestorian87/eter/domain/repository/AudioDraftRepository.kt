package com.nestorian87.eter.domain.repository

import com.nestorian87.eter.domain.model.AudioDraft
import java.io.File

interface AudioDraftRepository {
    suspend fun importAudio(uriString: String): AudioDraft

    fun createRecordingFile(): File

    suspend fun readAudioDraft(
        file: File,
        preferredDisplayName: String? = null,
        recordedAtEpochMs: Long? = null,
    ): AudioDraft

    fun delete(file: File?)
}
