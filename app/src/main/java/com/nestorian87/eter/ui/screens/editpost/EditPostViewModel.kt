package com.nestorian87.eter.ui.screens.editpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.model.EditPostDraft
import com.nestorian87.eter.domain.model.EditPostDraftCategory
import com.nestorian87.eter.domain.model.Post
import com.nestorian87.eter.domain.model.PostStatus
import com.nestorian87.eter.domain.model.UpdatePostPayload
import com.nestorian87.eter.domain.repository.AudioDraftRepository
import com.nestorian87.eter.domain.repository.EditPostDraftRepository
import com.nestorian87.eter.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditPostViewModel.Factory::class)
class EditPostViewModel @AssistedInject constructor(
    @Assisted val postId: Long,
    private val postRepository: PostRepository,
    private val editPostDraftRepository: EditPostDraftRepository,
    private val audioDraftRepository: AudioDraftRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()
    private var processingPollingJob: Job? = null
    private var categoriesSearchJob: Job? = null
    private var localDraftSaveJob: Job? = null

    init {
        stopProcessingPolling()
        localDraftSaveJob?.cancel()
        _uiState.value = EditPostUiState(postId = postId)
        loadPost(postId = postId, refreshForm = true)
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it.copy(
                title = value.take(MAX_TITLE_LENGTH),
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onTextChanged(value: String) {
        _uiState.update {
            it.copy(
                text = value,
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onOriginAuthorNameChanged(value: String) {
        _uiState.update {
            it.copy(
                originAuthorName = value.take(MAX_AUTHOR_LENGTH),
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update {
            it.copy(
                description = value,
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onCategorySearchChanged(value: String) {
        _uiState.update {
            it.copy(
                categorySearchQuery = value.take(MAX_CATEGORY_SEARCH_LENGTH),
                errorMessage = null,
            )
        }
        scheduleCategorySearch()
    }

    fun onCategorySelected(category: EditPostCategoryUiState) {
        val currentState = _uiState.value
        val isAlreadySelected = currentState.selectedCategories.any { it.categoryId == category.categoryId }

        if (isAlreadySelected) {
            onCategoryRemoved(category.categoryId)
            return
        }

        if (currentState.selectedCategories.size >= EditPostUiDefaults.CATEGORY_LIMIT) {
            _uiState.update {
                it.copy(errorMessage = EditPostUiMessage.Validation.CATEGORY_LIMIT_REACHED)
            }
            return
        }

        _uiState.update {
            it.copy(
                selectedCategories = (it.selectedCategories + category).distinctBy(EditPostCategoryUiState::categoryId),
                availableCategories = it.availableCategories.filterNot { item ->
                    item.categoryId == category.categoryId
                },
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onCategoryRemoved(categoryId: Long) {
        _uiState.update {
            it.copy(
                selectedCategories = it.selectedCategories.filterNot { category ->
                    category.categoryId == categoryId
                },
                errorMessage = null,
                isSaved = false,
            )
        }
        scheduleLocalDraftSave()
        scheduleCategorySearch()
    }

    fun onCopyrightConfirmedChanged(checked: Boolean) {
        _uiState.update {
            it.copy(
                isCopyrightConfirmed = checked,
                errorMessage = null,
            )
        }
        scheduleLocalDraftSave()
    }

    fun onSaveClick() {
        val currentState = _uiState.value
        val postId = currentState.postId ?: return

        val title = currentState.title.trim()
        val text = currentState.text.trim()
        val description = currentState.description.trim().takeIf { it.isNotEmpty() }
        val originAuthorName = currentState.originAuthorName.trim().takeIf { it.isNotEmpty() }

        val validationError = when {
            title.isBlank() -> EditPostUiMessage.Validation.TITLE_REQUIRED
            text.isBlank() -> EditPostUiMessage.Validation.TEXT_REQUIRED
            !currentState.isCopyrightConfirmed ->
                EditPostUiMessage.Validation.COPYRIGHT_CONFIRMATION_REQUIRED
            else -> null
        }

        if (validationError != null) {
            _uiState.update {
                it.copy(errorMessage = validationError)
            }
            return
        }

        viewModelScope.launch {
            localDraftSaveJob?.cancel()
            editPostDraftRepository.saveDraft(currentState.toLocalDraftSnapshot(postId))
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    isSaved = false,
                )
            }
            runCatching {
                postRepository.updatePost(
                    postId = postId,
                    payload = UpdatePostPayload(
                        title = title,
                        text = text,
                        description = description,
                        originAuthorName = originAuthorName,
                        categoryIds = currentState.selectedCategories.map(EditPostCategoryUiState::categoryId),
                    ),
                )
            }.onSuccess { post ->
                editPostDraftRepository.deleteDraft(postId)
                applyPost(post = post, refreshForm = true, localDraft = null)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isSaved = true,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toEditPostUiMessage(),
                    )
                }
                if ((error as? com.nestorian87.eter.domain.model.PostException)
                        ?.primaryReason == com.nestorian87.eter.domain.model.PostException.Reason.POST_IS_STILL_PROCESSING
                ) {
                    startProcessingPolling(postId)
                }
            }
        }
    }

    fun onReplaceAudioPicked(uri: Uri?) {
        val postId = _uiState.value.postId ?: return
        if (uri == null || !_uiState.value.canReplaceAudio) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isReplacingAudio = true,
                    errorMessage = null,
                    isSaved = false,
                )
            }
            runCatching {
                val draft = audioDraftRepository.importAudio(uri.toString())
                try {
                    postRepository.replacePostAudio(
                        postId = postId,
                        audioFile = draft.file,
                    )
                } finally {
                    audioDraftRepository.delete(draft.file)
                }
            }.onSuccess { post ->
                applyPost(post = post, refreshForm = false)
                _uiState.update {
                    it.copy(isReplacingAudio = false)
                }
                if (post.status == PostStatus.PROCESSING) {
                    startProcessingPolling(postId)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isReplacingAudio = false,
                        errorMessage = error.toEditPostUiMessage(),
                    )
                }
            }
        }
    }

    fun onRetryProcessingClick() {
        val postId = _uiState.value.postId ?: return
        loadPost(postId = postId, refreshForm = false)
    }

    private fun loadPost(
        postId: Long,
        refreshForm: Boolean,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingPost = true,
                    errorMessage = null,
                )
            }
            runCatching {
                postRepository.getPost(postId = postId)
            }.onSuccess { post ->
                val localDraft = editPostDraftRepository.getDraft(postId)
                applyPost(
                    post = post,
                    refreshForm = refreshForm,
                    localDraft = localDraft,
                )
                _uiState.update {
                    it.copy(isLoadingPost = false)
                }
                if (post.status == PostStatus.PROCESSING) {
                    startProcessingPolling(postId)
                } else {
                    stopProcessingPolling()
                    searchCategories(query = _uiState.value.categorySearchQuery)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingPost = false,
                        errorMessage = error.toEditPostUiMessage(),
                    )
                }
            }
        }
    }

    private fun applyPost(
        post: Post,
        refreshForm: Boolean,
        localDraft: EditPostDraft? = null,
    ) {
        _uiState.update { current ->
            val serverSelectedCategories = if (refreshForm) {
                post.categories.map { category ->
                    EditPostCategoryUiState(
                        categoryId = category.categoryId,
                        categoryName = category.categoryName,
                    )
                }
            } else {
                current.selectedCategories
            }

            current.copy(
                postId = post.postId,
                postStatus = post.status,
                title = if (refreshForm) {
                    localDraft?.title ?: post.title.orEmpty()
                } else {
                    current.title
                },
                text = if (refreshForm) {
                    localDraft?.text ?: post.text.orEmpty()
                } else {
                    current.text
                },
                originAuthorName = if (refreshForm) {
                    localDraft?.originAuthorName ?: post.originAuthorName.orEmpty()
                } else {
                    current.originAuthorName
                },
                description = if (refreshForm) {
                    localDraft?.description ?: post.description.orEmpty()
                } else {
                    current.description
                },
                audioDisplayName = post.audioFileName ?: current.audioDisplayName,
                audioFileUrl = post.audioFileUrl,
                selectedCategories = if (refreshForm && localDraft != null) {
                    localDraft.selectedCategories.map { category ->
                        EditPostCategoryUiState(
                            categoryId = category.categoryId,
                            categoryName = category.categoryName,
                        )
                    }
                } else {
                    serverSelectedCategories
                },
                isCopyrightConfirmed = if (refreshForm) {
                    localDraft?.isCopyrightConfirmed ?: current.isCopyrightConfirmed
                } else {
                    current.isCopyrightConfirmed
                },
                errorMessage = null,
            )
        }
    }

    private fun startProcessingPolling(postId: Long) {
        if (processingPollingJob?.isActive == true) {
            return
        }

        processingPollingJob = viewModelScope.launch {
            while (true) {
                delay(PROCESSING_POLL_INTERVAL_MS)
                val post = runCatching {
                    postRepository.getPost(postId = postId)
                }.getOrElse { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.toEditPostUiMessage())
                    }
                    continue
                }

                val localDraft = editPostDraftRepository.getDraft(postId)
                applyPost(
                    post = post,
                    refreshForm = true,
                    localDraft = localDraft,
                )
                if (post.status != PostStatus.PROCESSING) {
                    _uiState.update {
                        it.copy(isLoadingPost = false)
                    }
                    searchCategories(query = _uiState.value.categorySearchQuery)
                    stopProcessingPolling()
                    return@launch
                }
            }
        }
    }

    private fun stopProcessingPolling() {
        processingPollingJob?.cancel()
        processingPollingJob = null
    }

    private fun scheduleCategorySearch() {
        if (!_uiState.value.isEditable) {
            return
        }

        categoriesSearchJob?.cancel()
        categoriesSearchJob = viewModelScope.launch {
            delay(CATEGORY_SEARCH_DEBOUNCE_MS)
            searchCategories(query = _uiState.value.categorySearchQuery)
        }
    }

    private fun scheduleLocalDraftSave() {
        val postId = _uiState.value.postId ?: return
        localDraftSaveJob?.cancel()
        localDraftSaveJob = viewModelScope.launch {
            delay(LOCAL_DRAFT_SAVE_DEBOUNCE_MS)
            editPostDraftRepository.saveDraft(_uiState.value.toLocalDraftSnapshot(postId))
        }
    }

    private fun EditPostUiState.toLocalDraftSnapshot(postId: Long): EditPostDraft = EditPostDraft(
        postId = postId,
        title = title,
        text = text,
        originAuthorName = originAuthorName,
        description = description,
        selectedCategories = selectedCategories.map { category ->
            EditPostDraftCategory(
                categoryId = category.categoryId,
                categoryName = category.categoryName,
            )
        },
        isCopyrightConfirmed = isCopyrightConfirmed,
        updatedAtEpochMs = System.currentTimeMillis(),
    )

    private fun searchCategories(query: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingCategories = true)
            }
            runCatching {
                postRepository.getCategories(
                    search = query.trim().takeIf { value -> value.isNotEmpty() },
                )
            }.onSuccess { categories ->
                val selectedIds = _uiState.value.selectedCategories.map(EditPostCategoryUiState::categoryId).toSet()
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        availableCategories = categories
                            .filterNot { category -> selectedIds.contains(category.categoryId) }
                            .map { category ->
                                EditPostCategoryUiState(
                                    categoryId = category.categoryId,
                                    categoryName = category.categoryName,
                                )
                            },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        errorMessage = error.toEditPostUiMessage(),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        stopProcessingPolling()
        categoriesSearchJob?.cancel()
        localDraftSaveJob?.cancel()
        super.onCleared()
    }

    @AssistedFactory
    interface Factory {
        fun create(postId: Long): EditPostViewModel
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 200
        const val MAX_AUTHOR_LENGTH = 200
        const val MAX_CATEGORY_SEARCH_LENGTH = 120
        const val CATEGORY_SEARCH_DEBOUNCE_MS = 300L
        const val LOCAL_DRAFT_SAVE_DEBOUNCE_MS = 500L
        const val PROCESSING_POLL_INTERVAL_MS = 2_500L
    }
}
