package com.nestorian87.eter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.nestorian87.eter.ui.screens.auth.EmailConfirmationScreen
import com.nestorian87.eter.ui.screens.auth.ForgotPasswordScreen
import com.nestorian87.eter.ui.screens.auth.LoginScreen
import com.nestorian87.eter.ui.screens.auth.RegisterScreen
import com.nestorian87.eter.ui.screens.create.LyricSyncScreen
import com.nestorian87.eter.ui.screens.create.PublishScreen
import com.nestorian87.eter.ui.screens.create.RecordAudioScreen
import com.nestorian87.eter.ui.screens.feed.FeedScreen
import com.nestorian87.eter.ui.screens.favorites.FavoritesScreen
import com.nestorian87.eter.ui.screens.notifications.NotificationsScreen
import com.nestorian87.eter.ui.screens.post.PostScreen
import com.nestorian87.eter.ui.screens.profile.EditProfileScreen
import com.nestorian87.eter.ui.screens.profile.FollowersScreen
import com.nestorian87.eter.ui.screens.profile.ProfileScreen
import com.nestorian87.eter.ui.screens.profile.SettingsScreen
import com.nestorian87.eter.ui.screens.profile.SubscriptionManagementScreen
import com.nestorian87.eter.ui.screens.create.CreateEntryScreen
import com.nestorian87.eter.ui.screens.subscriptions.SubscriptionsScreen

@Composable
fun EterNavigation(
    navigationState: EterNavigationState,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = navigationState.currentBackStack,
        onBack = { navigationState.pop() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<LoginKey> {
                LoginScreen(modifier = modifier)
            }
            entry<RegisterKey> {
                RegisterScreen(modifier = modifier)
            }
            entry<EmailConfirmationKey> {
                EmailConfirmationScreen(modifier = modifier)
            }
            entry<ForgotPasswordKey> {
                ForgotPasswordScreen(modifier = modifier)
            }
            entry<FeedKey> {
                FeedScreen(modifier = modifier)
            }
            entry<SubscriptionsKey> {
                SubscriptionsScreen(modifier = modifier)
            }
            entry<CreateKey> {
                CreateEntryScreen(modifier = modifier)
            }
            entry<FavoritesKey> {
                FavoritesScreen(modifier = modifier)
            }
            entry<ProfileKey> {
                ProfileScreen(modifier = modifier)
            }
            entry<RecordAudioKey> {
                RecordAudioScreen(modifier = modifier)
            }
            entry<PublishKey> {
                PublishScreen(modifier = modifier)
            }
            entry<LyricSyncKey> {
                LyricSyncScreen(modifier = modifier)
            }
            entry<PostKey> {
                PostScreen(modifier = modifier)
            }
            entry<UserProfileKey> {
                ProfileScreen(modifier = modifier)
            }
            entry<FollowersKey> {
                FollowersScreen(modifier = modifier)
            }
            entry<NotificationsKey> {
                NotificationsScreen(modifier = modifier)
            }
            entry<EditProfileKey> {
                EditProfileScreen(modifier = modifier)
            }
            entry<SettingsKey> {
                SettingsScreen(modifier = modifier)
            }
            entry<ManageSubscriptionKey> {
                SubscriptionManagementScreen(modifier = modifier)
            }
        },
    )
}
