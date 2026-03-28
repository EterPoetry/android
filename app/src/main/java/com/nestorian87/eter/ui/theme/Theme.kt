package com.nestorian87.eter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ClayLight,
    secondary = ClayLight,
    tertiary = ClayLight,
    background = Espresso,
    surface = Mocha,
    surfaceVariant = ColorPalette.darkSurfaceVariant,
    onPrimary = Espresso,
    onSecondary = Espresso,
    onTertiary = Espresso,
    onBackground = Cream,
    onSurface = Cream,
    onSurfaceVariant = Taupe,
    outline = Taupe.copy(alpha = 0.32f),
)

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    secondary = Terracotta,
    tertiary = BurntClay,
    background = WarmBeige,
    surface = Paper,
    surfaceVariant = ColorPalette.lightSurfaceVariant,
    onPrimary = Paper,
    onSecondary = Paper,
    onTertiary = Paper,
    onBackground = Umber,
    onSurface = Umber,
    onSurfaceVariant = Mushroom,
    outline = Linen,
)

@Composable
fun EterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private object ColorPalette {
    val lightSurfaceVariant = Color(0xFFF2EDE5)
    val darkSurfaceVariant = Color(0xFF352922)
}
