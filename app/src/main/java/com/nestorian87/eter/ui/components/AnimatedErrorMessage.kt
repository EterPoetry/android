package com.nestorian87.eter.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun AnimatedErrorMessage(
    @StringRes messageResId: Int?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = messageResId != null,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
            expandVertically(animationSpec = tween(durationMillis = 220)),
        exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
            shrinkVertically(animationSpec = tween(durationMillis = 160)),
    ) {
        Column {
            Spacer(modifier = Modifier.height(EterSpacing.medium))
            Text(
                text = stringResource(messageResId ?: return@Column),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(end = EterSpacing.xSmall),
            )
        }
    }
}
