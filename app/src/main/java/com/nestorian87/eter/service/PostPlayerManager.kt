package com.nestorian87.eter.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.nestorian87.eter.MainActivity
import com.nestorian87.eter.ui.navigation.EterDeepLink
import com.nestorian87.eter.data.local.datastore.PostPlayerSnapshotStore
import com.nestorian87.eter.data.local.datastore.toDomain
import com.nestorian87.eter.data.local.datastore.toPlayerSnapshot
import com.nestorian87.eter.data.repository.PostInteractionStore
import com.nestorian87.eter.di.qualifier.MainDispatcher
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.ui.components.PostPlayerUiState
import com.nestorian87.eter.domain.repository.PostRepository
import com.nestorian87.eter.ui.components.resolveRemoteAssetUrl
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

@androidx.annotation.OptIn(markerClass = [UnstableApi::class])
@Singleton
class PostPlayerManager @Inject constructor(
    @ApplicationContext context: Context,
    private val postRepository: PostRepository,
    private val postInteractionStore: PostInteractionStore,
    private val snapshotStore: PostPlayerSnapshotStore,
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
    private val playerMutex = Mutex()
    private val appContext = context.applicationContext
    private val _uiState = MutableStateFlow(PostPlayerUiState())
    private val player = ExoPlayer.Builder(context.applicationContext)
        .setSeekBackIncrementMs(NOTIFICATION_SEEK_INCREMENT_MS)
        .setSeekForwardIncrementMs(NOTIFICATION_SEEK_INCREMENT_MS)
        .build()
        .apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true,
        )
        setHandleAudioBecomingNoisy(true)
        volume = DEFAULT_VOLUME
        playWhenReady = false
    }
    private val mediaSession = MediaSession.Builder(appContext, player)
        .setSessionActivity(buildSessionActivity(postId = null))
        .build()
    private val playerServiceIntent = Intent(appContext, PlayerService::class.java)
    private var playbackLoopJob: Job? = null
    private var currentListenToken: String? = null

    val uiState: StateFlow<PostPlayerUiState> = _uiState.asStateFlow()
    val session: MediaSession
        get() = mediaSession
    val isPlaybackOngoing: Boolean
        get() = player.isPlaying

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { current ->
                    current.copy(isPlaying = isPlaying)
                }
                if (isPlaying) {
                    startPlaybackLoop()
                } else {
                    maybeStopPlaybackLoop()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        _uiState.update { current ->
                            current.copy(isPlaying = false)
                        }
                    }

                    Player.STATE_BUFFERING -> {
                        _uiState.update { current ->
                            current.copy(
                                currentTimeSeconds = player.currentPosition.coerceAtLeast(0L) / 1000f,
                            )
                        }
                    }

                    Player.STATE_READY -> {
                        _uiState.update { current ->
                            current.copy(durationSeconds = player.duration.coerceAtLeast(0L) / 1000f)
                        }
                    }

                    Player.STATE_ENDED -> {
                        scope.launch {
                            handlePlaybackEnded()
                        }
                    }
                }
            }
        })
        scope.launch {
            restoreSnapshot()
        }
        scope.launch {
            postInteractionStore.overrides.collectLatest {
                val activePost = _uiState.value.activePost ?: return@collectLatest
                _uiState.update { current ->
                    current.copy(activePost = postInteractionStore.apply(activePost))
                }
            }
        }
    }

    fun togglePostPlayback(post: Post) {
        scope.launch {
            playerMutex.withLock {
                val currentPost = _uiState.value.activePost
                if (currentPost?.postId == post.postId) {
                    updateActivePost(post)
                    if (_uiState.value.isPlaying) {
                        pausePlaybackLocked()
                    } else {
                        resumePlaybackLocked()
                    }
                } else {
                    playPostLocked(post = post, startPositionMs = null)
                }
            }
        }
    }

    fun playPost(post: Post, startPositionMs: Long? = null) {
        scope.launch {
            playerMutex.withLock {
                playPostLocked(post, startPositionMs)
            }
        }
    }

    fun updatePostMetadata(post: Post) {
        scope.launch {
            playerMutex.withLock {
                if (_uiState.value.activePost?.postId != post.postId) {
                    return@withLock
                }
                updateActivePost(post)
            }
        }
    }

    fun pausePlayback() {
        scope.launch {
            playerMutex.withLock {
                pausePlaybackLocked()
            }
        }
    }

    fun seekToPercent(percent: Float) {
        scope.launch {
            playerMutex.withLock {
                val durationMs = player.duration.takeIf { it > 0L }
                    ?: (_uiState.value.durationSeconds * 1000L).roundToInt().toLong()
                if (durationMs <= 0L) {
                    return@withLock
                }
                val targetPositionMs = (durationMs * (percent.coerceIn(0f, 100f) / 100f)).roundToInt().toLong()
                player.seekTo(targetPositionMs)
                updateCurrentPositionState(targetPositionMs)
                if (_uiState.value.isPlaying) {
                    _uiState.update { current ->
                        current.copy(lastReportedPositionMs = targetPositionMs)
                    }
                }
                persistSnapshot()
                if (_uiState.value.isPlaying) {
                    startPlaybackLoop()
                } else {
                    syncListenProgress(force = true)
                }
            }
        }
    }

    fun closePlayer() {
        scope.launch {
            playerMutex.withLock {
                finalizeCurrentSession(clearActivePost = true)
            }
        }
    }

    private suspend fun playPostLocked(post: Post, startPositionMs: Long?) {
        val audioUrl = post.audioFileUrl ?: return
        val currentPostId = _uiState.value.activePost?.postId
        if (currentPostId == post.postId) {
            updateActivePost(post)
            if (startPositionMs != null) {
                player.seekTo(startPositionMs)
                updateCurrentPositionState(startPositionMs)
                _uiState.update { current ->
                    current.copy(lastReportedPositionMs = startPositionMs)
                }
                persistSnapshot()
            }
            if (!_uiState.value.isPlaying) {
                resumePlaybackLocked()
            } else {
                startPlaybackLoop()
            }
            return
        }
        if (currentPostId != null) {
            finalizeCurrentSession(clearActivePost = false)
        }
        _uiState.update { current ->
            current.copy(
                activePost = postInteractionStore.apply(post),
                isPlaying = false,
                currentTimeSeconds = 0f,
                durationSeconds = post.audioDurationSeconds?.toFloat() ?: 0f,
                isStartingPlayback = true,
                isFinalizingListen = false,
                currentSessionId = null,
                lastReportedPositionMs = 0L,
            )
        }
        postInteractionStore.ensureListenBase(post)
        currentListenToken = null
        ensurePlayerServiceStarted()
        player.stop()
        player.setMediaItem(buildMediaItem(post, audioUrl))
        player.prepare()
        if (startPositionMs != null && startPositionMs > 0L) {
            player.seekTo(startPositionMs)
            updateCurrentPositionState(startPositionMs)
        } else {
            player.seekTo(0L)
        }
        persistSnapshot()
        ensureListenSession()
        runCatching {
            player.playWhenReady = true
            player.play()
        }
        _uiState.update { current ->
            current.copy(isStartingPlayback = false)
        }
        startPlaybackLoop()
    }

    private suspend fun pausePlaybackLocked() {
        maybeStopPlaybackLoop()
        player.pause()
        updateCurrentPositionState(player.currentPosition.coerceAtLeast(0L))
        syncListenProgress(force = true)
        persistSnapshot()
    }

    private suspend fun resumePlaybackLocked() {
        if (_uiState.value.activePost == null) {
            return
        }
        ensureListenSession()
        ensurePlayerServiceStarted()
        runCatching {
            player.playWhenReady = true
            player.play()
        }
        startPlaybackLoop()
        persistSnapshot()
    }

    private suspend fun ensureListenSession() {
        val activePost = _uiState.value.activePost ?: return
        if (currentListenToken != null) {
            return
        }
        val sessionId = _uiState.value.currentSessionId ?: UUID.randomUUID().toString()
        _uiState.update { current ->
            current.copy(currentSessionId = sessionId)
        }
        runCatching {
            postRepository.startListen(
                postId = activePost.postId,
                sessionId = sessionId,
            )
        }.onSuccess { result ->
            currentListenToken = result.token
            _uiState.update { current ->
                current.copy(
                    durationSeconds = (result.trackDurationMs / 1000f).takeIf { it > 0f }
                        ?: current.durationSeconds,
                )
            }
        }
        persistSnapshot()
    }

    private suspend fun syncListenProgress(force: Boolean) {
        val token = currentListenToken ?: return
        val activePost = _uiState.value.activePost ?: return
        val positionMs = player.currentPosition.coerceAtLeast(0L)
        val lastReportedPositionMs = _uiState.value.lastReportedPositionMs
        if (!force && positionMs - lastReportedPositionMs < LISTEN_PROGRESS_DELTA_MS) {
            return
        }
        _uiState.update { current ->
            current.copy(isSyncingListenProgress = true)
        }
        runCatching {
            postRepository.updateListenProgress(
                postId = activePost.postId,
                token = token,
                positionMs = positionMs.toInt(),
            )
        }.onSuccess {
            _uiState.update { current ->
                current.copy(lastReportedPositionMs = positionMs)
            }
        }
        _uiState.update { current ->
            current.copy(isSyncingListenProgress = false)
        }
    }

    private suspend fun finalizeCurrentSession(clearActivePost: Boolean) {
        maybeStopPlaybackLoop()
        player.pause()
        syncListenProgress(force = true)
        val activePost = _uiState.value.activePost
        val token = currentListenToken
        if (activePost != null && token != null) {
            _uiState.update { current ->
                current.copy(isFinalizingListen = true)
            }
            runCatching {
                postRepository.endListen(
                    postId = activePost.postId,
                    token = token,
                    positionMs = player.currentPosition.coerceAtLeast(0L).toInt(),
                    sessionId = _uiState.value.currentSessionId,
                )
            }.onSuccess { result ->
                if (result.counted) {
                    postInteractionStore.incrementListens(activePost.postId)
                    _uiState.update { current ->
                        current.copy(
                            countedListenVersion = current.countedListenVersion + 1,
                            lastCountedPostId = activePost.postId,
                        )
                    }
                }
            }
        }
        currentListenToken = null
        _uiState.update { current ->
            current.copy(
                isFinalizingListen = false,
                currentSessionId = null,
                lastReportedPositionMs = 0L,
                activePost = if (clearActivePost) null else current.activePost,
                isPlaying = false,
                currentTimeSeconds = if (clearActivePost) 0f else current.currentTimeSeconds,
            )
        }
        if (clearActivePost) {
            player.stop()
            player.clearMediaItems()
            snapshotStore.clear()
            stopPlayerService()
        } else {
            persistSnapshot()
        }
    }

    private suspend fun handlePlaybackEnded() {
        playerMutex.withLock {
            val activePost = _uiState.value.activePost ?: return
            finalizeCurrentSession(clearActivePost = false)
            player.seekTo(0L)
            _uiState.update { current ->
                current.copy(
                    activePost = activePost,
                    currentTimeSeconds = 0f,
                    isPlaying = false,
                )
            }
            persistSnapshot()
        }
    }

    private fun startPlaybackLoop() {
        playbackLoopJob?.cancel()
        playbackLoopJob = scope.launch {
            var lastProgressSyncAt = 0L
            while (player.isPlaying) {
                val positionMs = player.currentPosition.coerceAtLeast(0L)
                updateCurrentPositionState(positionMs)
                if (positionMs - lastProgressSyncAt >= LISTEN_PROGRESS_INTERVAL_MS) {
                    syncListenProgress(force = false)
                    persistSnapshot()
                    lastProgressSyncAt = positionMs
                }
                delay(PLAYBACK_TICK_MS)
            }
        }
    }

    private fun maybeStopPlaybackLoop() {
        playbackLoopJob?.cancel()
        playbackLoopJob = null
    }

    private fun updateCurrentPositionState(positionMs: Long) {
        _uiState.update { current ->
            current.copy(
                currentTimeSeconds = positionMs / 1000f,
                durationSeconds = player.duration.takeIf { it > 0L }?.div(1000f) ?: current.durationSeconds,
            )
        }
    }

    @UnstableApi
    private fun updateActivePost(post: Post) {
        val audioUrl = post.audioFileUrl
        if (audioUrl != null && player.currentMediaItem != null) {
            player.replaceMediaItem(
                0,
                buildMediaItem(post, audioUrl),
            )
        }
        mediaSession.setSessionActivity(buildSessionActivity(post.postId))
        _uiState.update { current ->
            current.copy(activePost = postInteractionStore.apply(post))
        }
        persistSnapshotAsync()
    }

    @UnstableApi
    private suspend fun restoreSnapshot() {
        val snapshot = snapshotStore.read() ?: return
        val restoredPost = runCatching {
            postRepository.getPost(snapshot.post.postId)
        }.getOrNull()?.takeIf { it.audioFileUrl != null } ?: snapshot.post.toDomain()

        if (restoredPost.audioFileUrl == null) {
            snapshotStore.clear()
            return
        }
        postInteractionStore.ensureListenBase(restoredPost)
        _uiState.update { current ->
            current.copy(
                activePost = postInteractionStore.apply(restoredPost),
                isPlaying = false,
                currentTimeSeconds = snapshot.currentTimeSeconds,
                durationSeconds = restoredPost.audioDurationSeconds?.toFloat() ?: current.durationSeconds,
                volume = snapshot.volume,
                isMuted = snapshot.isMuted,
            )
        }
        player.setMediaItem(buildMediaItem(restoredPost, restoredPost.audioFileUrl))
        player.prepare()
        player.seekTo((snapshot.currentTimeSeconds * 1000f).roundToInt().toLong())
        player.playWhenReady = false
        player.volume = if (snapshot.isMuted) 0f else snapshot.volume
        mediaSession.setSessionActivity(buildSessionActivity(restoredPost.postId))
    }

    private fun persistSnapshotAsync() {
        scope.launch {
            persistSnapshot()
        }
    }

    private suspend fun persistSnapshot() {
        val current = _uiState.value
        val activePost = current.activePost ?: return
        snapshotStore.save(
            activePost.toPlayerSnapshot(
                currentTimeSeconds = current.currentTimeSeconds,
                volume = current.volume,
                isMuted = current.isMuted,
            ),
        )
    }

    private fun buildMediaItem(post: Post, audioUrl: String): MediaItem = MediaItem.Builder()
        .setMediaId(post.postId.toString())
        .setUri(audioUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(post.title.orEmpty().ifBlank { "Eter" })
                .setArtist(post.author?.name ?: post.originAuthorName ?: "Eter")
                .setDisplayTitle(post.title.orEmpty().ifBlank { "Eter" })
                .setSubtitle(post.author?.name ?: post.originAuthorName ?: "Eter")
                .setArtworkUri(post.author?.photo.resolveRemoteAssetUrl()?.let(android.net.Uri::parse))
                .build(),
        )
        .build()

    private fun ensurePlayerServiceStarted() {
        ContextCompat.startForegroundService(appContext, playerServiceIntent)
    }

    private fun stopPlayerService() {
        appContext.stopService(playerServiceIntent)
    }

    private fun buildSessionActivity(postId: Long?): PendingIntent {
        val deepLinkUri = postId?.let { EterDeepLink.postUri(postId = it) }
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (deepLinkUri != null) {
                action = Intent.ACTION_VIEW
                data = deepLinkUri
            }
        }
        val requestCode = (postId ?: 0L).toInt()
        return PendingIntent.getActivity(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val DEFAULT_VOLUME = 1f
        private const val PLAYBACK_TICK_MS = 500L
        private const val LISTEN_PROGRESS_INTERVAL_MS = 5_000L
        private const val LISTEN_PROGRESS_DELTA_MS = 1_000L
        private const val NOTIFICATION_SEEK_INCREMENT_MS = 10_000L
    }
}
