package com.nestorian87.eter.ui.navigation

import androidx.navigation3.runtime.NavKey
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
sealed interface EterNavKey : NavKey

@Serializable
sealed interface TopLevelNavKey : EterNavKey

@Serializable
data object FeedKey : TopLevelNavKey

@Serializable
data object SubscriptionsKey : TopLevelNavKey

@Serializable
data object CreateKey : TopLevelNavKey

@Serializable
data object FavoritesKey : TopLevelNavKey

@Serializable
data object ProfileKey : TopLevelNavKey

@Serializable
data object LoginKey : EterNavKey

@Serializable
data object RegisterKey : EterNavKey

@Serializable
data object EmailConfirmationKey : EterNavKey

@Serializable
data object ForgotPasswordKey : EterNavKey

@Serializable
data class RecordAudioKey(
    val draftId: String = UUID.randomUUID().toString(),
) : EterNavKey

@Serializable
data class PublishKey(
    val audioFilePath: String,
) : EterNavKey

@Serializable
data class LyricSyncKey(
    val postId: String,
) : EterNavKey

@Serializable
data class PostKey(
    val postId: String,
) : EterNavKey

@Serializable
data class UserProfileKey(
    val userId: String,
) : EterNavKey

@Serializable
data class FollowersKey(
    val userId: String,
    val initialTab: String = "followers",
) : EterNavKey

@Serializable
data object NotificationsKey : EterNavKey

@Serializable
data object EditProfileKey : EterNavKey

@Serializable
data object SettingsKey : EterNavKey

@Serializable
data object ManageSubscriptionKey : EterNavKey
