package com.nestorian87.eter.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.graphics.vector.ImageVector
import com.nestorian87.eter.R

enum class TopLevelDestination(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val navKey: TopLevelNavKey,
) {
    FEED(R.string.feed, Icons.Outlined.Home, FeedKey),
    SUBSCRIPTIONS(R.string.following, Icons.Outlined.RemoveRedEye, SubscriptionsKey),
    CREATE(R.string.create, Icons.Rounded.Add, CreateKey),
    FAVORITES(R.string.favorites, Icons.Outlined.FavoriteBorder, FavoritesKey),
    PROFILE(R.string.profile, Icons.Outlined.AccountCircle, ProfileKey),
}
