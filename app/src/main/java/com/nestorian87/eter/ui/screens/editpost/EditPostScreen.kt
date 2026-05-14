package com.nestorian87.eter.ui.screens.editpost

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.PostStatus
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.components.AudioPlaybackCard
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.LoadingOverlay
import com.nestorian87.eter.ui.components.ScreenIntroCard
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun EditPostScreen(
    postId: Long,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: EditPostViewModel = hiltViewModel<EditPostViewModel, EditPostViewModel.Factory> { factory ->
        factory.create(postId)
    },
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val replaceAudioLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        viewModel.onReplaceAudioPicked(uri)
    }

    EditPostScreenContent(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onBackClick,
        onTitleChanged = viewModel::onTitleChanged,
        onTextChanged = viewModel::onTextChanged,
        onOriginAuthorNameChanged = viewModel::onOriginAuthorNameChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onCategorySearchChanged = viewModel::onCategorySearchChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onCategoryRemoved = viewModel::onCategoryRemoved,
        onCopyrightConfirmedChanged = viewModel::onCopyrightConfirmedChanged,
        onSaveClick = viewModel::onSaveClick,
        onRetryProcessingClick = viewModel::onRetryProcessingClick,
        onReplaceAudioClick = { replaceAudioLauncher.launch("audio/*") },
    )
}

@Composable
private fun EditPostScreenContent(
    uiState: EditPostUiState,
    onBackClick: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onOriginAuthorNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategorySearchChanged: (String) -> Unit,
    onCategorySelected: (EditPostCategoryUiState) -> Unit,
    onCategoryRemoved: (Long) -> Unit,
    onCopyrightConfirmedChanged: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onRetryProcessingClick: () -> Unit,
    onReplaceAudioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isCategoryPickerOpen by rememberSaveable { mutableStateOf(false) }

    FormScreenScaffold(
        modifier = modifier,
    ) {
        Spacer(modifier = Modifier.height(EterSpacing.small))
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                EditPostHeader()
                Spacer(modifier = Modifier.height(EterSpacing.xLarge))
                EditPostAudioStatusCard(
                    uiState = uiState,
                    onRetryProcessingClick = onRetryProcessingClick,
                    onReplaceAudioClick = onReplaceAudioClick,
                )
                Spacer(modifier = Modifier.height(EterSpacing.xLarge))
                if (uiState.isProcessing) {
                    EditPostProcessingStateCard()
                } else {
                    EditPostForm(
                        uiState = uiState,
                        onTitleChanged = onTitleChanged,
                        onTextChanged = onTextChanged,
                        onOriginAuthorNameChanged = onOriginAuthorNameChanged,
                        onDescriptionChanged = onDescriptionChanged,
                        onOpenCategoryPicker = { isCategoryPickerOpen = true },
                        onCategoryRemoved = onCategoryRemoved,
                        onCopyrightConfirmedChanged = onCopyrightConfirmedChanged,
                    )
                }
                Spacer(modifier = Modifier.height(EterSpacing.xLarge))
                AnimatedErrorMessage(
                    messageResId = uiState.errorMessage?.toMessageResId(),
                )
                Spacer(modifier = Modifier.height(EterSpacing.large))
                EditPostActionButtons(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onSaveClick = onSaveClick,
                )
                Spacer(modifier = Modifier.height(EterSpacing.xxLarge))
            }

            LoadingOverlay(
                isVisible = uiState.isLoadingPost,
                modifier = Modifier.matchParentSize(),
            )
        }
    }

    if (isCategoryPickerOpen) {
        EditPostCategoryPickerSheet(
            uiState = uiState,
            onDismissRequest = {},
            onCategorySearchChanged = onCategorySearchChanged,
            onCategorySelected = onCategorySelected,
            onCategoryRemoved = onCategoryRemoved,
        )
    }
}

@Composable
private fun EditPostHeader() {
    ScreenIntroCard(
        icon = Icons.Rounded.Edit,
        title = stringResource(R.string.publish_title),
        subtitle = stringResource(R.string.publish_subtitle),
    )
}

@Composable
private fun EditPostAudioStatusCard(
    uiState: EditPostUiState,
    onRetryProcessingClick: () -> Unit,
    onReplaceAudioClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.medium),
        ) {
            if (!uiState.isProcessing && uiState.audioFileUrl != null) {
                EditPostAudioPreviewPlayer(
                    title = stringResource(R.string.publish_audio_default_name),
                    audioUrl = uiState.audioFileUrl,
                    canReplaceAudio = uiState.canReplaceAudio,
                    onReplaceAudioClick = onReplaceAudioClick,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (uiState.isProcessing) {
                                Icons.Rounded.HourglassTop
                            } else {
                                Icons.Rounded.MusicNote
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(EterSpacing.medium))
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.publish_audio_default_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                when (uiState.postStatus) {
                                    PostStatus.PROCESSING -> R.string.publish_audio_processing_status
                                    PostStatus.PUBLISHED -> R.string.publish_audio_published_status
                                    PostStatus.DRAFT -> R.string.publish_audio_draft_status
                                    null -> R.string.publish_audio_loading_status
                                },
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (uiState.isProcessing) {
                        EditPostAudioActionChip(
                            icon = Icons.Rounded.Refresh,
                            text = stringResource(R.string.publish_check_status_cta),
                            contentDescription = stringResource(R.string.publish_retry_processing),
                            onClick = onRetryProcessingClick,
                        )
                    } else if (uiState.canReplaceAudio) {
                        EditPostAudioActionChip(
                            icon = Icons.Rounded.UploadFile,
                            text = stringResource(R.string.publish_replace_audio_cta),
                            contentDescription = stringResource(R.string.publish_replace_audio_cta),
                            onClick = onReplaceAudioClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditPostAudioPreviewPlayer(
    title: String,
    audioUrl: String,
    canReplaceAudio: Boolean,
    onReplaceAudioClick: () -> Unit,
) {
    AudioPlaybackCard(
        mediaUri = audioUrl,
        title = title,
        playContentDescription = stringResource(R.string.publish_play_audio),
        pauseContentDescription = stringResource(R.string.publish_pause_audio),
        trailingContent = if (canReplaceAudio) {
            {
                EditPostAudioActionChip(
                    icon = Icons.Rounded.UploadFile,
                    text = stringResource(R.string.publish_replace_audio_cta),
                    contentDescription = stringResource(R.string.publish_replace_audio_cta),
                    onClick = onReplaceAudioClick,
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun EditPostAudioActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun EditPostProcessingStateCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EterSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Rounded.HourglassTop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp),
            )
            Spacer(modifier = Modifier.height(EterSpacing.large))
            Text(
                text = stringResource(R.string.publish_processing_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(EterSpacing.xSmall))
            Text(
                text = stringResource(R.string.publish_processing_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EditPostForm(
    uiState: EditPostUiState,
    onTitleChanged: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onOriginAuthorNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onOpenCategoryPicker: () -> Unit,
    onCategoryRemoved: (Long) -> Unit,
    onCopyrightConfirmedChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EterSpacing.xLarge),
    ) {
        EditPostSection(
            title = stringResource(R.string.publish_section_content_title),
            subtitle = stringResource(R.string.publish_section_content_subtitle),
        ) {
            EditPostBoxTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                label = stringResource(R.string.publish_field_title),
                placeholder = stringResource(R.string.publish_field_title_placeholder),
            )
            Spacer(modifier = Modifier.height(EterSpacing.large))
            EditPostBoxTextField(
                value = uiState.text,
                onValueChange = onTextChanged,
                label = stringResource(R.string.publish_field_text),
                placeholder = stringResource(R.string.publish_field_text_placeholder),
                singleLine = false,
                minHeight = 156.dp,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
            )
            Spacer(modifier = Modifier.height(EterSpacing.large))
            EditPostBoxTextField(
                value = uiState.originAuthorName,
                onValueChange = onOriginAuthorNameChanged,
                label = stringResource(R.string.publish_field_author),
                placeholder = stringResource(R.string.publish_field_author_placeholder),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            Spacer(modifier = Modifier.height(EterSpacing.large))
            EditPostBoxTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                label = stringResource(R.string.publish_field_description),
                placeholder = stringResource(R.string.publish_field_description_placeholder),
                singleLine = false,
                minHeight = 96.dp,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
            )
        }
        EditPostSection(
            title = stringResource(R.string.publish_section_categories_title),
            subtitle = stringResource(R.string.publish_section_categories_subtitle),
        ) {
            EditPostCategoriesSection(
                uiState = uiState,
                onOpenPicker = onOpenCategoryPicker,
                onCategoryRemoved = onCategoryRemoved,
            )
        }
        EditPostSection(
            title = stringResource(R.string.publish_section_rights_title),
            subtitle = stringResource(R.string.publish_section_rights_subtitle),
        ) {
            CopyrightRow(
                checked = uiState.isCopyrightConfirmed,
                onCheckedChange = onCopyrightConfirmedChanged,
            )
            if (uiState.isSaved) {
                Spacer(modifier = Modifier.height(EterSpacing.medium))
                Text(
                    text = stringResource(R.string.publish_saved_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EditPostCategoriesSection(
    uiState: EditPostUiState,
    onOpenPicker: () -> Unit,
    onCategoryRemoved: (Long) -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.publish_categories_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.publish_categories_selected_count,
                    uiState.selectedCountLabel,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(EterSpacing.medium))
        Text(
            text = stringResource(R.string.publish_categories_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(EterSpacing.small))
        EditPostCategoryPickerTrigger(
            selectionSummary = uiState.selectedCategories.joinToString { it.categoryName },
            onClick = onOpenPicker,
        )
        if (uiState.selectedCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(EterSpacing.medium))
            Text(
                text = stringResource(R.string.publish_categories_selected_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(EterSpacing.small))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
                verticalArrangement = Arrangement.spacedBy(EterSpacing.small),
            ) {
                uiState.selectedCategories.forEach { category ->
                    EditPostSelectableChip(
                        text = category.categoryName,
                        selected = true,
                        onClick = { onCategoryRemoved(category.categoryId) },
                    )
                }
            }
        }
    }
}


@EterScreenPreviews
@Composable
private fun EditPostScreenProcessingPreview() {
    EterPreview {
        EditPostScreenContent(
            uiState = EditPostUiState(
                postId = 1L,
                postStatus = PostStatus.PROCESSING,
                isLoadingPost = false,
                audioDisplayName = "Аудіозапис",
            ),
            onBackClick = {},
            onTitleChanged = {},
            onTextChanged = {},
            onOriginAuthorNameChanged = {},
            onDescriptionChanged = {},
            onCategorySearchChanged = {},
            onCategorySelected = {},
            onCategoryRemoved = {},
            onCopyrightConfirmedChanged = {},
            onSaveClick = {},
            onRetryProcessingClick = {},
            onReplaceAudioClick = {},
        )
    }
}

@EterScreenPreviews
@Composable
private fun EditPostScreenDraftPreview() {
    EterPreview {
        EditPostScreenContent(
            uiState = EditPostUiState(
                postId = 1L,
                postStatus = PostStatus.DRAFT,
                isLoadingPost = false,
                title = "Ніч над містом",
                text = "Текст вірша, який уже підготовлений до публікації.",
                originAuthorName = "Леся Українка",
                description = "Короткий опис або контекст для слухача.",
                audioDisplayName = "Запис 05.04.2026 18:37",
                selectedCategories = listOf(
                    EditPostCategoryUiState(1, "Лірика"),
                    EditPostCategoryUiState(2, "Кохання"),
                ),
                availableCategories = listOf(
                    EditPostCategoryUiState(3, "Меланхолія"),
                    EditPostCategoryUiState(4, "Сучасне"),
                ),
                isCopyrightConfirmed = true,
            ),
            onBackClick = {},
            onTitleChanged = {},
            onTextChanged = {},
            onOriginAuthorNameChanged = {},
            onDescriptionChanged = {},
            onCategorySearchChanged = {},
            onCategorySelected = {},
            onCategoryRemoved = {},
            onCopyrightConfirmedChanged = {},
            onSaveClick = {},
            onRetryProcessingClick = {},
            onReplaceAudioClick = {},
        )
    }
}
