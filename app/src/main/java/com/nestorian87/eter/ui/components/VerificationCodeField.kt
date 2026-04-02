package com.nestorian87.eter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun VerificationCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    codeLength: Int = 6,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    autoFocus: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequesters = remember(codeLength) {
        List(codeLength) { FocusRequester() }
    }
    val sanitizedValue = value.filter(Char::isDigit).take(codeLength)
    val cellValues = remember(codeLength) {
        mutableStateListOf<Char?>().apply {
            repeat(codeLength) {
                add(null)
            }
        }
    }
    var focusedIndex by remember { mutableIntStateOf(0) }
    var lastDispatchedValue by remember { mutableIntStateOf(Int.MIN_VALUE) }

    LaunchedEffect(sanitizedValue) {
        val currentJoinedValue = cellValues.joinToString(separator = "") { it?.toString().orEmpty() }
        if (sanitizedValue == currentJoinedValue || sanitizedValue.hashCode() == lastDispatchedValue) {
            return@LaunchedEffect
        }

        repeat(codeLength) { index ->
            cellValues[index] = sanitizedValue.getOrNull(index)
        }
    }

    LaunchedEffect(autoFocus, enabled, readOnly) {
        if (autoFocus && enabled && !readOnly) {
            val targetIndex = firstEmptyIndex(cellValues).coerceAtMost(codeLength - 1)
            focusRequesters[targetIndex].requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EterSpacing.small),
    ) {
        repeat(codeLength) { index ->
            val digit = cellValues[index]?.toString().orEmpty()
            val isFocused = focusedIndex == index
            val fieldValue = TextFieldValue(
                text = digit,
                selection = TextRange(digit.length),
            )

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(72.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(
                            alpha = if (isFocused && enabled) 0.7f else 0.35f,
                        ),
                        shape = MaterialTheme.shapes.small,
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small,
                    )
                    .clickable(
                        enabled = enabled,
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        focusedIndex = index
                        focusRequesters[index].requestFocus()
                        keyboardController?.show()
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { updatedValue ->
                        val nextDigits = updatedValue.text.filter(Char::isDigit)
                        if (nextDigits.isEmpty()) {
                            updateDigitAtIndex(
                                cellValues = cellValues,
                                index = index,
                                digit = null,
                            )
                            dispatchCurrentValue(
                                cellValues = cellValues,
                                onValueChange = onValueChange,
                                onDispatch = { dispatchedValue ->
                                    lastDispatchedValue = dispatchedValue.hashCode()
                                },
                            )
                            return@BasicTextField
                        }

                        applyDigitsFromIndex(
                            cellValues = cellValues,
                            startIndex = index,
                            digits = nextDigits,
                            codeLength = codeLength,
                        )
                        dispatchCurrentValue(
                            cellValues = cellValues,
                            onValueChange = onValueChange,
                            onDispatch = { dispatchedValue ->
                                lastDispatchedValue = dispatchedValue.hashCode()
                            },
                        )

                        val nextFocusIndex = (index + nextDigits.length).coerceAtMost(codeLength - 1)
                        focusedIndex = nextFocusIndex
                        if (index + nextDigits.length < codeLength) {
                            focusRequesters[nextFocusIndex].requestFocus()
                            keyboardController?.show()
                        } else {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        }
                    },
                    enabled = enabled,
                    readOnly = readOnly,
                    modifier = Modifier
                        .width(48.dp)
                        .height(72.dp)
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                focusedIndex = index
                            }
                        }
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type != KeyEventType.KeyDown || keyEvent.key != Key.Backspace) {
                                return@onPreviewKeyEvent false
                            }

                            if (digit.isNotEmpty()) {
                                updateDigitAtIndex(
                                    cellValues = cellValues,
                                    index = index,
                                    digit = null,
                                )
                                dispatchCurrentValue(
                                    cellValues = cellValues,
                                    onValueChange = onValueChange,
                                    onDispatch = { dispatchedValue ->
                                        lastDispatchedValue = dispatchedValue.hashCode()
                                    },
                                )
                            } else if (index > 0) {
                                val previousIndex = index - 1
                                updateDigitAtIndex(
                                    cellValues = cellValues,
                                    index = previousIndex,
                                    digit = null,
                                )
                                dispatchCurrentValue(
                                    cellValues = cellValues,
                                    onValueChange = onValueChange,
                                    onDispatch = { dispatchedValue ->
                                        lastDispatchedValue = dispatchedValue.hashCode()
                                    },
                                )
                                focusedIndex = previousIndex
                                focusRequesters[previousIndex].requestFocus()
                                keyboardController?.show()
                            }
                            true
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = if (index == codeLength - 1) ImeAction.Done else ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            val nextIndex = (index + 1).coerceAtMost(codeLength - 1)
                            focusedIndex = nextIndex
                            focusRequesters[nextIndex].requestFocus()
                            keyboardController?.show()
                        },
                        onDone = {
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                        },
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            innerTextField()
                        }
                    },
                )
            }
        }
    }
}

private fun applyDigitsFromIndex(
    cellValues: MutableList<Char?>,
    startIndex: Int,
    digits: String,
    codeLength: Int,
) {
    digits.forEachIndexed { offset, digit ->
        val targetIndex = startIndex + offset
        if (targetIndex >= codeLength) {
            return@forEachIndexed
        }
        cellValues[targetIndex] = digit
    }
}

private fun updateDigitAtIndex(
    cellValues: MutableList<Char?>,
    index: Int,
    digit: Char?,
) {
    cellValues[index] = digit
}

private fun dispatchCurrentValue(
    cellValues: List<Char?>,
    onValueChange: (String) -> Unit,
    onDispatch: (String) -> Unit,
) {
    val currentValue = cellValues.joinToString(separator = "") { it?.toString().orEmpty() }
    onDispatch(currentValue)
    onValueChange(currentValue)
}

private fun firstEmptyIndex(cellValues: List<Char?>): Int {
    val firstEmptyIndex = cellValues.indexOfFirst { it == null }
    if (firstEmptyIndex >= 0) {
        return firstEmptyIndex
    }
    return cellValues.lastIndex.coerceAtLeast(0)
}
