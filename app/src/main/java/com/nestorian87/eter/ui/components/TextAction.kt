package com.nestorian87.eter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextAction(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Text(
        text = text,
        modifier = modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
