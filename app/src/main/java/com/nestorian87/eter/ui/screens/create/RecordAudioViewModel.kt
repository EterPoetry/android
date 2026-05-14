package com.nestorian87.eter.ui.screens.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.model.AudioDraft
import com.nestorian87.eter.domain.repository.AudioDraftRepository
import com.nestorian87.eter.domain.repository.AudioRecorderRepository
import com.nestorian87.eter.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordAudioViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val audioDraftRepository: AudioDraftRepository,
    private val audioRecorderRepository: AudioRecorderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordAudioUiState())
    val uiState: StateFlow<RecordAudioUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<RecordAudioEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var recordedDraft: AudioDraft? = null
    private var uploadedDraft: AudioDraft? = null
    private var recordingTimerJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching {
                postRepository.getPublicConfig()
            }.onSuccess { config ->
                _uiState.update { current ->
                    current.copy(maxDurationMinutes = config.recording.freeDurationLimitMinutes)
                }
            }
        }
    }

    fun onSourceModeSelected(mode: RecordAudioSourceMode) {
        val snapshot = _uiState.value
        if (!snapshot.canSwitchSource || snapshot.sourceMode == mode) {
            return
        }

        _uiState.update { current ->
            current.copy(
                sourceMode = mode,
                errorMessage = null,
            )
        }
    }

    fun onPickAudioRequested(uri: Uri?) {
        if (uri == null || _uiState.value.isRecording) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImportingAudio = true,
                    errorMessage = null,
                )
            }
            runCatching {
                audioDraftRepository.importAudio(uri.toString())
            }.onSuccess { draft ->
                replaceUploadedDraft(draft)
                _uiState.update {
                    it.copy(
                        sourceMode = RecordAudioSourceMode.UPLOAD,
                        isImportingAudio = false,
                        uploadedAudio = draft.toUiState(),
                        errorMessage = null,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isImportingAudio = false,
                        errorMessage = RecordAudioUiMessage.Validation.AUDIO_IMPORT_FAILED,
                    )
                }
            }
        }
    }

    fun onStartRecordingClick() {
        val snapshot = _uiState.value
        if (snapshot.isRecording || snapshot.isUploadingPost || snapshot.isImportingAudio) {
            return
        }

        clearRecordedDraft()

        runCatching {
            audioRecorderRepository.startRecording(audioDraftRepository.createRecordingFile())
            startRecordingTimer(maxDurationMs = snapshot.maxDurationMs)
        }.onSuccess {
            _uiState.update {
                it.copy(
                    sourceMode = RecordAudioSourceMode.RECORD,
                    isRecording = true,
                    recordingDurationMs = 0L,
                    recordedAudio = null,
                    errorMessage = null,
                )
            }
        }.onFailure {
            _uiState.update {
                it.copy(errorMessage = RecordAudioUiMessage.Validation.RECORDING_START_FAILED)
            }
        }
    }

    fun onRecordPermissionDenied() {
        _uiState.update {
            it.copy(errorMessage = RecordAudioUiMessage.Validation.RECORD_PERMISSION_REQUIRED)
        }
    }

    fun onStopRecordingClick() {
        if (!_uiState.value.isRecording) {
            return
        }

        finishRecording()
    }

    fun onContinueClick() {
        val draft = selectedDraftForCurrentSource()
        if (draft == null) {
            _uiState.update {
                it.copy(errorMessage = RecordAudioUiMessage.Validation.SELECT_AUDIO_FIRST)
            }
            return
        }

        if (_uiState.value.isRecording || _uiState.value.isUploadingPost) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingPost = true,
                    errorMessage = null,
                )
            }
            runCatching {
                postRepository.createPost(audioFile = draft.file)
            }.onSuccess { post ->
                clearAllDrafts()
                _uiState.update {
                    it.copy(
                        isUploadingPost = false,
                        recordedAudio = null,
                        uploadedAudio = null,
                        recordingDurationMs = 0L,
                        errorMessage = null,
                    )
                }
                effectChannel.send(
                    RecordAudioEffect.NavigateToEditPost(postId = post.postId),
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUploadingPost = false,
                        errorMessage = error.toRecordAudioUiMessage(),
                    )
                }
            }
        }
    }

    fun onCancelClick() {
        if (_uiState.value.isRecording) {
            cancelActiveRecording()
        }

        clearAllDrafts()
        _uiState.update {
            RecordAudioUiState(maxDurationMinutes = it.maxDurationMinutes)
        }
    }

    fun onClearSelectedAudioClick() {
        if (_uiState.value.isRecording) {
            return
        }

        when (_uiState.value.sourceMode) {
            RecordAudioSourceMode.RECORD -> {
                clearRecordedDraft()
                _uiState.update {
                    it.copy(
                        recordedAudio = null,
                        errorMessage = null,
                    )
                }
            }

            RecordAudioSourceMode.UPLOAD -> {
                clearUploadedDraft()
                _uiState.update {
                    it.copy(
                        uploadedAudio = null,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun finishRecording() {
        stopRecordingTimer()

        viewModelScope.launch {
            runCatching {
                val recordedAtEpochMs = System.currentTimeMillis()
                val recordedFile = audioRecorderRepository.stopRecording()
                audioDraftRepository.readAudioDraft(
                    file = recordedFile,
                    recordedAtEpochMs = recordedAtEpochMs,
                )
            }.onSuccess { draft ->
                replaceRecordedDraft(draft)
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        recordingDurationMs = draft.durationMs,
                        recordedAudio = draft.toUiState(),
                        errorMessage = null,
                    )
                }
            }.onFailure {
                clearRecordedDraft()
                _uiState.update {
                    it.copy(
                        isRecording = false,
                        recordingDurationMs = 0L,
                        recordedAudio = null,
                        errorMessage = RecordAudioUiMessage.Validation.RECORDING_STOP_FAILED,
                    )
                }
            }
        }
    }

    private fun startRecordingTimer(maxDurationMs: Long) {
        stopRecordingTimer()
        recordingTimerJob = viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            while (isActive) {
                val elapsedMs = System.currentTimeMillis() - startedAt
                val cappedElapsedMs = elapsedMs.coerceAtMost(maxDurationMs)
                _uiState.update {
                    it.copy(recordingDurationMs = cappedElapsedMs)
                }

                if (cappedElapsedMs >= maxDurationMs) {
                    finishRecording()
                    return@launch
                }

                delay(RECORDING_PROGRESS_INTERVAL_MS)
            }
        }
    }

    private fun stopRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }

    private fun cancelActiveRecording() {
        stopRecordingTimer()
        audioRecorderRepository.cancelRecording()
    }

    private fun selectedDraftForCurrentSource(): AudioDraft? = when (_uiState.value.sourceMode) {
        RecordAudioSourceMode.RECORD -> recordedDraft
        RecordAudioSourceMode.UPLOAD -> uploadedDraft
    }

    private fun replaceRecordedDraft(draft: AudioDraft) {
        val previousDraft = recordedDraft
        recordedDraft = draft
        if (previousDraft?.file?.absolutePath != draft.file.absolutePath) {
            audioDraftRepository.delete(previousDraft?.file)
        }
    }

    private fun replaceUploadedDraft(draft: AudioDraft) {
        val previousDraft = uploadedDraft
        uploadedDraft = draft
        if (previousDraft?.file?.absolutePath != draft.file.absolutePath) {
            audioDraftRepository.delete(previousDraft?.file)
        }
    }

    private fun clearRecordedDraft() {
        val previousDraft = recordedDraft
        recordedDraft = null
        audioDraftRepository.delete(previousDraft?.file)
    }

    private fun clearUploadedDraft() {
        val previousDraft = uploadedDraft
        uploadedDraft = null
        audioDraftRepository.delete(previousDraft?.file)
    }

    private fun clearAllDrafts() {
        clearRecordedDraft()
        clearUploadedDraft()
    }

    override fun onCleared() {
        stopRecordingTimer()
        audioRecorderRepository.cancelRecording()
        clearAllDrafts()
        super.onCleared()
    }

    private fun AudioDraft.toUiState(): SelectedAudioUiState = SelectedAudioUiState(
        displayName = displayName,
        durationMs = durationMs,
        localFilePath = file.absolutePath,
        recordedAtEpochMs = recordedAtEpochMs,
    )

    private companion object {
        const val RECORDING_PROGRESS_INTERVAL_MS = 200L
    }
}
