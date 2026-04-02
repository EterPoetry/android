package com.nestorian87.eter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun GoogleAuthButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 62.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.small,
            )
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = EterSpacing.large, vertical = EterSpacing.large),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.alpha(if (isLoading) 0f else 1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier
                    .width(EterSpacing.xLarge)
                    .height(EterSpacing.xLarge),
            )
            Spacer(modifier = Modifier.width(EterSpacing.medium))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        if (isLoading) {
            EterLoadingIndicator(
                color = MaterialTheme.colorScheme.primary,
                dotSize = 8.dp,
                spacing = 6.dp,
            )
        }
    }
}
