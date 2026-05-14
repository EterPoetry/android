package com.nestorian87.eter.ui.screens.create

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.components.AudioPlaybackCard
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.LoadingOverlay
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.ScreenIntroCard
import com.nestorian87.eter.ui.components.SecondaryActionButton
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecordAudioScreen(
    modifier: Modifier = Modifier,
    onNavigateToEditPost: (Long) -> Unit = {},
    viewModel: RecordAudioViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    val audioPickerLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        viewModel.onPickAudioRequested(uri)
    }
    val recordPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            viewModel.onStartRecordingClick()
        } else {
            viewModel.onRecordPermissionDenied()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is RecordAudioEffect.NavigateToEditPost -> onNavigateToEditPost(effect.postId)
            }
        }
    }

    RecordAudioScreenContent(
        uiState = uiState,
        modifier = modifier,
        onSourceModeSelected = viewModel::onSourceModeSelected,
        onStartRecordingClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.onStartRecordingClick()
            } else {
                recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onStopRecordingClick = viewModel::onStopRecordingClick,
        onChooseFileClick = { audioPickerLauncher.launch("audio/*") },
        onClearAudioClick = viewModel::onClearSelectedAudioClick,
        onCancelClick = viewModel::onCancelClick,
        onContinueClick = viewModel::onContinueClick,
    )
}

@Composable
private fun RecordAudioScreenContent(
    uiState: RecordAudioUiState,
    onSourceModeSelected: (RecordAudioSourceMode) -> Unit,
    onStartRecordingClick: () -> Unit,
    onStopRecordingClick: () -> Unit,
    onChooseFileClick: () -> Unit,
    onClearAudioClick: () -> Unit,
    onCancelClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FormScreenScaffold(
        modifier = modifier,
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(EterSpacing.medium),
            ) {
                SecondaryActionButton(
                    text = stringResource(R.string.create_audio_cancel_cta),
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isUploadingPost && !uiState.isImportingAudio,
                    onClick = onCancelClick,
                )
                PrimaryActionButton(
                    text = stringResource(R.string.create_audio_continue_cta),
                    modifier = Modifier.weight(1f),
                    enabled = uiState.canContinue,
                    isLoading = uiState.isUploadingPost,
                    onClick = onContinueClick,
                )
            }
        },
    ) {
        Spacer(modifier = Modifier.height(EterSpacing.small))
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                RecordAudioHeader(maxDurationMinutes = uiState.maxDurationMinutes)
                Spacer(modifier = Modifier.height(EterSpacing.xLarge))
                SourceModeSwitcher(
                    selectedMode = uiState.sourceMode,
                    canSwitch = uiState.canSwitchSource,
                    onModeSelected = onSourceModeSelected,
                )
                Spacer(modifier = Modifier.height(EterSpacing.large))
                when (uiState.sourceMode) {
                    RecordAudioSourceMode.RECORD -> RecordingPanel(
                        uiState = uiState,
                        onStartRecordingClick = onStartRecordingClick,
                        onStopRecordingClick = onStopRecordingClick,
                        onClearAudioClick = onClearAudioClick,
                    )

                    RecordAudioSourceMode.UPLOAD -> UploadPanel(
                        uiState = uiState,
                        onChooseFileClick = onChooseFileClick,
                        onClearAudioClick = onClearAudioClick,
                    )
                }
                AnimatedErrorMessage(
                    messageResId = uiState.errorMessage?.toMessageResId(),
                )
            }

            LoadingOverlay(
                isVisible = uiState.isImportingAudio,
                modifier = Modifier.matchParentSize(),
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun RecordAudioHeader(
    maxDurationMinutes: Int,
) {
    ScreenIntroCard(
        icon = Icons.Rounded.MusicNote,
        title = stringResource(R.string.create_audio_title),
        subtitle = stringResource(
            R.string.create_audio_limit_subtitle,
            maxDurationMinutes,
        ),
        supportingText = stringResource(R.string.create_audio_record_hint),
    )
}

@Composable
private fun SourceModeSwitcher(
    selectedMode: RecordAudioSourceMode,
    canSwitch: Boolean,
    onModeSelected: (RecordAudioSourceMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                shape = MaterialTheme.shapes.medium,
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                shape = MaterialTheme.shapes.medium,
            )
            .padding(4.dp),
    ) {
        SourceModeTab(
            text = stringResource(R.string.create_audio_tab_record),
            selected = selectedMode == RecordAudioSourceMode.RECORD,
            enabled = canSwitch,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(RecordAudioSourceMode.RECORD) },
        )
        SourceModeTab(
            text = stringResource(R.string.create_audio_tab_upload),
            selected = selectedMode == RecordAudioSourceMode.UPLOAD,
            enabled = canSwitch,
            modifier = Modifier.weight(1f),
            onClick = { onModeSelected(RecordAudioSourceMode.UPLOAD) },
        )
    }
}

@Composable
private fun SourceModeTab(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(vertical = EterSpacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun RecordingPanel(
    uiState: RecordAudioUiState,
    onStartRecordingClick: () -> Unit,
    onStopRecordingClick: () -> Unit,
    onClearAudioClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
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
                .padding(horizontal = EterSpacing.large, vertical = EterSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                uiState.isRecording -> {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.large))
                    Text(
                        text = stringResource(
                            R.string.create_audio_recording_progress,
                            formatDuration(uiState.recordingDurationMs),
                            formatDuration(uiState.maxDurationMs),
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.large))
                    CompactPrimaryAction(
                        text = stringResource(R.string.create_audio_stop_recording_cta),
                        icon = Icons.Rounded.Stop,
                        onClick = onStopRecordingClick,
                    )
                }

                uiState.recordedAudio != null -> {
                    AudioPreviewRow(
                        audio = uiState.recordedAudio,
                        onClearAudioClick = onClearAudioClick,
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.medium))
                    CompactOutlineAction(
                        text = stringResource(R.string.create_audio_record_again_cta),
                        onClick = onStartRecordingClick,
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.large))
                    Text(
                        text = stringResource(R.string.create_audio_record_prompt),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.xSmall))
                    Text(
                        text = stringResource(R.string.create_audio_record_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(EterSpacing.large))
                    CompactPrimaryAction(
                        text = stringResource(R.string.create_audio_start_recording_cta),
                        icon = Icons.Rounded.FiberManualRecord,
                        onClick = onStartRecordingClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadPanel(
    uiState: RecordAudioUiState,
    onChooseFileClick: () -> Unit,
    onClearAudioClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
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
                .padding(horizontal = EterSpacing.large, vertical = EterSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.uploadedAudio == null) {
                Icon(
                    imageVector = Icons.Rounded.UploadFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(52.dp),
                )
                Spacer(modifier = Modifier.height(EterSpacing.large))
                Text(
                    text = stringResource(R.string.create_audio_upload_prompt),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(EterSpacing.xSmall))
                Text(
                    text = stringResource(R.string.create_audio_upload_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(EterSpacing.large))
                CompactPrimaryAction(
                    text = stringResource(R.string.create_audio_pick_file_cta),
                    icon = Icons.Rounded.UploadFile,
                    onClick = onChooseFileClick,
                )
            } else {
                AudioPreviewRow(
                    audio = uiState.uploadedAudio,
                    onClearAudioClick = onClearAudioClick,
                )
                Spacer(modifier = Modifier.height(EterSpacing.medium))
                CompactOutlineAction(
                    text = stringResource(R.string.create_audio_choose_another_cta),
                    onClick = onChooseFileClick,
                )
            }
        }
    }
}

@Composable
private fun AudioPreviewRow(
    audio: SelectedAudioUiState,
    onClearAudioClick: () -> Unit,
) {
    AudioPlaybackCard(
        mediaUri = java.io.File(audio.localFilePath).toURI().toString(),
        title = resolveSelectedAudioTitle(audio),
        playContentDescription = stringResource(R.string.create_audio_play_audio),
        pauseContentDescription = stringResource(R.string.create_audio_pause_audio),
        durationFallbackMs = audio.durationMs,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.36f),
                shape = MaterialTheme.shapes.small,
            )
            .heightIn(min = 120.dp),
        subtitle = { durationMs ->
            Text(
                text = stringResource(
                    R.string.create_audio_preview_duration,
                    formatDuration(durationMs),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(
                onClick = onClearAudioClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.create_audio_remove_audio),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun resolveSelectedAudioTitle(audio: SelectedAudioUiState): String {
    val recordedAtEpochMs = audio.recordedAtEpochMs
    if (recordedAtEpochMs != null) {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val formattedDate = formatter.format(
            Instant.ofEpochMilli(recordedAtEpochMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
        )
        return stringResource(R.string.create_audio_recorded_audio_name, formattedDate)
    }

    return audio.displayName ?: stringResource(R.string.create_audio_default_audio_name)
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(durationMs.coerceAtLeast(0L))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun CompactPrimaryAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = EterSpacing.xLarge, vertical = EterSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(EterSpacing.xSmall))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun CompactOutlineAction(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.48f),
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = EterSpacing.medium, vertical = 10.dp),
        )
    }
}

@EterScreenPreviews
@Composable
private fun RecordAudioScreenPreview() {
    EterPreview {
        RecordAudioScreenContent(
            uiState = RecordAudioUiState(
                sourceMode = RecordAudioSourceMode.RECORD,
                maxDurationMinutes = 7,
            ),
            onSourceModeSelected = {},
            onStartRecordingClick = {},
            onStopRecordingClick = {},
            onChooseFileClick = {},
            onClearAudioClick = {},
            onCancelClick = {},
            onContinueClick = {},
        )
    }
}

@EterScreenPreviews
@Composable
private fun RecordAudioScreenSelectedPreview() {
    EterPreview {
        RecordAudioScreenContent(
            uiState = RecordAudioUiState(
                sourceMode = RecordAudioSourceMode.UPLOAD,
                uploadedAudio = SelectedAudioUiState(
                    displayName = null,
                    durationMs = 440_000L,
                    localFilePath = "/tmp/recording.m4a",
                    recordedAtEpochMs = 1_775_193_420_000L,
                ),
            ),
            onSourceModeSelected = {},
            onStartRecordingClick = {},
            onStopRecordingClick = {},
            onChooseFileClick = {},
            onClearAudioClick = {},
            onCancelClick = {},
            onContinueClick = {},
        )
    }
}
