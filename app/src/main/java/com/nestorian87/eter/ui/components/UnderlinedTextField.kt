package com.nestorian87.eter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLength: Int? = null,
    trailing: (@Composable () -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
        )
        Spacer(modifier = Modifier.height(EterSpacing.xSmall))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { updatedValue ->
                    onValueChange(updatedValue.take(maxLength ?: Int.MAX_VALUE))
                },
                modifier = Modifier.weight(1f),
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (trailing != null) {
                Spacer(modifier = Modifier.width(EterSpacing.small))
                trailing()
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(
            thickness = 1.6.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.85f),
        )
    }
}
