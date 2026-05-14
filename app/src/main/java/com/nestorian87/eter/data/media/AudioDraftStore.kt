package com.nestorian87.eter.data.media

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.nestorian87.eter.di.qualifier.IoDispatcher
import com.nestorian87.eter.domain.model.AudioDraft
import com.nestorian87.eter.domain.repository.AudioDraftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

@Singleton
class AudioDraftStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AudioDraftRepository {
    override suspend fun importAudio(uriString: String): AudioDraft = withContext(ioDispatcher) {
        val uri = uriString.toUri()
        val resolver = context.contentResolver
        val displayName = resolver.queryDisplayName(uri)
        val extension = displayName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
            ?: resolver.getType(uri)?.substringAfterLast('/', "")
            ?: "audio"
        val targetFile = File(draftsDirectory(), "import-${System.currentTimeMillis()}.$extension")

        resolver.openInputStream(uri)?.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Could not open selected audio stream")

        readAudioDraft(
            file = targetFile,
            preferredDisplayName = displayName,
        )
    }

    override fun createRecordingFile(): File = File(
        draftsDirectory().also(File::mkdirs),
        "recording-${System.currentTimeMillis()}.m4a",
    )

    override suspend fun readAudioDraft(
        file: File,
        preferredDisplayName: String?,
        recordedAtEpochMs: Long?,
    ): AudioDraft = withContext(ioDispatcher) {
        val retriever = MediaMetadataRetriever()
        val durationMs = try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: 0L
        } finally {
            retriever.release()
        }

        AudioDraft(
            file = file,
            displayName = preferredDisplayName,
            durationMs = durationMs,
            recordedAtEpochMs = recordedAtEpochMs,
        )
    }

    override fun delete(file: File?) {
        if (file == null) {
            return
        }

        runCatching {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun draftsDirectory(): File = File(context.cacheDir, "post-audio-drafts")

    private fun ContentResolver.queryDisplayName(uri: Uri): String? {
        return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex == -1 || !cursor.moveToFirst()) {
                return@use null
            }

            cursor.getString(nameIndex)
        }
    }
}
