package com.nestorian87.eter.ui.screens.favorites

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Favorites",
        description = "This screen will show saved and liked posts, including offline-ready content.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun FavoritesScreenPreview() {
    EterPreview {
        FavoritesScreen()
    }
}
