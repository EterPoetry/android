package com.nestorian87.eter.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.PlaceholderScreen
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews

@Composable
fun EditProfileScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Edit Profile",
        description = "This screen will let users update their public profile, avatar, and bio.",
        modifier = modifier,
    )
}

@EterScreenPreviews
@Composable
private fun EditProfileScreenPreview() {
    EterPreview {
        EditProfileScreen()
    }
}
