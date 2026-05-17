package com.nestorian87.eter.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class PostPlayerSnapshotStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    suspend fun read(): PostPlayerSnapshot? =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[PlayerSnapshotKey] }
            .first()
            ?.let { encoded ->
                runCatching {
                    json.decodeFromString(PostPlayerSnapshot.serializer(), encoded)
                }.getOrNull()
            }

    suspend fun save(snapshot: PostPlayerSnapshot) {
        dataStore.edit { preferences ->
            preferences[PlayerSnapshotKey] = json.encodeToString(
                PostPlayerSnapshot.serializer(),
                snapshot,
            )
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(PlayerSnapshotKey)
        }
    }

    companion object {
        private val PlayerSnapshotKey = stringPreferencesKey("post_player_snapshot")
    }
}

@Serializable
data class PostPlayerSnapshot(
    val post: PersistedPostSnapshot,
    val currentTimeSeconds: Float,
    val volume: Float,
    val isMuted: Boolean,
)

@Serializable
data class PersistedPostSnapshot(
    val postId: Long,
    val title: String? = null,
    val description: String? = null,
    val text: String? = null,
    val audioFileName: String? = null,
    val audioFileUrl: String? = null,
    val audioDurationSeconds: Int? = null,
    val status: String,
    val listens: Int,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val originAuthorName: String? = null,
    val textSynchronization: List<PersistedPostTextSynchronization> = emptyList(),
    val categories: List<PersistedPostCategory> = emptyList(),
    val authorId: Long,
    val author: PersistedPostAuthor? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PersistedPostAuthor(
    val userId: Long,
    val name: String,
    val username: String,
    val photo: String? = null,
    val isPremium: Boolean = false,
)

@Serializable
data class PersistedPostCategory(
    val categoryId: Long,
    val categoryName: String,
)

@Serializable
data class PersistedPostTextSynchronization(
    val lineIndex: Int,
    val audioStartMomentMs: Int,
)

