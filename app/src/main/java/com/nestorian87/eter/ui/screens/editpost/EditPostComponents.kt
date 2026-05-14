package com.nestorian87.eter.ui.screens.editpost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.SecondaryActionButton
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun EditPostCategoryPickerTrigger(
    selectionSummary: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(EterSpacing.medium))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.publish_categories_picker_cta),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (selectionSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectionSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostCategoryPickerSheet(
    uiState: EditPostUiState,
    onDismissRequest: () -> Unit,
    onCategorySearchChanged: (String) -> Unit,
    onCategorySelected: (EditPostCategoryUiState) -> Unit,
    onCategoryRemoved: (Long) -> Unit,
) {
    val colors = rememberEditPostCategorySheetColors()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = colors.sheetContainer,
        scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colors.handle,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .imePadding()
                .padding(horizontal = EterSpacing.medium)
                .padding(bottom = EterSpacing.large),
        ) {
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
            Spacer(modifier = Modifier.height(EterSpacing.small))
            Text(
                text = stringResource(R.string.publish_categories_sheet_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(EterSpacing.small))
            EditPostSearchField(
                value = uiState.categorySearchQuery,
                onValueChange = onCategorySearchChanged,
                placeholder = stringResource(R.string.publish_categories_search_placeholder),
            )
            if (uiState.selectedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(EterSpacing.medium))
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(EterSpacing.small),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(EterSpacing.small),
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
            Spacer(modifier = Modifier.height(EterSpacing.large))
            if (uiState.selectedCategories.size >= EditPostUiDefaults.CATEGORY_LIMIT) {
                EditPostCategoryLimitNotice()
                Spacer(modifier = Modifier.height(EterSpacing.small))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
            ) {
                Text(
                    text = stringResource(R.string.publish_categories_results_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(EterSpacing.small))
                when {
                    uiState.isLoadingCategories -> {
                        Text(
                            text = stringResource(R.string.publish_categories_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                        )
                    }

                    uiState.availableCategories.isNotEmpty() -> {
                        EditPostCategoryResultList(
                            modifier = Modifier.weight(1f, fill = true),
                            categories = uiState.availableCategories,
                            isSelectionLimitReached = uiState.selectedCategories.size >= EditPostUiDefaults.CATEGORY_LIMIT,
                            onCategorySelected = onCategorySelected,
                        )
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.publish_categories_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditPostCategoryResultList(
    categories: List<EditPostCategoryUiState>,
    isSelectionLimitReached: Boolean,
    onCategorySelected: (EditPostCategoryUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = rememberEditPostCategorySheetColors()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.listContainer,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = colors.listBorder,
        ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
        ) {
            itemsIndexed(
                items = categories,
                key = { _, category -> category.categoryId },
            ) { index, category ->
                EditPostCategoryResultRow(
                    category = category,
                    enabled = !isSelectionLimitReached,
                    onClick = { onCategorySelected(category) },
                )
                if (index != categories.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                    )
                }
            }
        }
    }
}

@Composable
fun EditPostCategoryLimitNotice() {
    val colors = rememberEditPostCategorySheetColors()

    Surface(
        color = colors.noticeBackground,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            width = 1.dp,
            color = colors.noticeBorder,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(EterSpacing.small))
            Column {
                Text(
                    text = stringResource(R.string.publish_categories_limit_reached_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.publish_categories_limit_reached_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun EditPostCategoryResultRow(
    category: EditPostCategoryUiState,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = rememberEditPostCategorySheetColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Spacer(modifier = Modifier.width(EterSpacing.medium))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (enabled) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.publish_categories_add_cta),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Surface(
                    color = colors.disabledBadgeBackground,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(R.string.publish_categories_limit_reached_inline),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun EditPostSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(EterSpacing.medium))
        content()
    }
}

@Composable
fun CopyrightRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        Spacer(modifier = Modifier.width(EterSpacing.small))
        Text(
            text = stringResource(R.string.publish_copyright_confirmation_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable { onCheckedChange(!checked) },
        )
    }
}

@Composable
fun EditPostActionButtons(
    uiState: EditPostUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(EterSpacing.medium),
    ) {
        SecondaryActionButton(
            text = stringResource(R.string.publish_back_cta),
            modifier = Modifier.weight(1f),
            enabled = !uiState.isSubmitting && !uiState.isReplacingAudio,
            onClick = onBackClick,
        )
        PrimaryActionButton(
            text = stringResource(
                if (uiState.postStatus == com.nestorian87.eter.domain.model.PostStatus.PUBLISHED) {
                    R.string.publish_update_cta
                } else {
                    R.string.publish_submit_cta
                },
            ),
            modifier = Modifier.weight(1f),
            enabled = uiState.canSave,
            isLoading = uiState.isSubmitting,
            onClick = onSaveClick,
        )
    }
}

@Composable
fun EditPostBoxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 52.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Next,
    ),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )
        Spacer(modifier = Modifier.height(EterSpacing.xSmall))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    shape = MaterialTheme.shapes.small,
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.small),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight),
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = keyboardOptions,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
fun EditPostSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    val colors = rememberEditPostCategorySheetColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.inputBorder,
                shape = MaterialTheme.shapes.small,
            )
            .background(colors.inputBackground)
            .padding(horizontal = EterSpacing.medium, vertical = EterSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(EterSpacing.small))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
fun EditPostSelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = rememberEditPostCategorySheetColors()

    Surface(
        color = if (selected) {
            colors.selectedChipBackground
        } else {
            colors.idleChipBackground
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                colors.selectedChipBorder
            } else {
                colors.idleChipBorder
            },
        ),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                horizontal = EterSpacing.medium,
                vertical = 10.dp,
            ),
        )
    }
}

@Composable
fun rememberEditPostCategorySheetColors(): EditPostCategorySheetColors {
    return EditPostCategorySheetColors(
        sheetContainer = MaterialTheme.colorScheme.surface,
        handle = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f),
        listContainer = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        listBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        inputBackground = MaterialTheme.colorScheme.background,
        inputBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        selectedChipBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        selectedChipBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
        idleChipBackground = MaterialTheme.colorScheme.surface,
        idleChipBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.26f),
        noticeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        noticeBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        disabledBadgeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
    )
}

data class EditPostCategorySheetColors(
    val sheetContainer: Color,
    val handle: Color,
    val listContainer: Color,
    val listBorder: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val selectedChipBackground: Color,
    val selectedChipBorder: Color,
    val idleChipBackground: Color,
    val idleChipBorder: Color,
    val noticeBackground: Color,
    val noticeBorder: Color,
    val disabledBadgeBackground: Color,
)
