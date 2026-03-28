package com.nestorian87.eter.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light",
    showBackground = true,
    locale = "uk",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 412,
    heightDp = 917,
)
@Preview(
    name = "Dark",
    showBackground = true,
    locale = "uk",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 412,
    heightDp = 917,
)
annotation class EterScreenPreviews

@Composable
fun EterPreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    EterTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}
