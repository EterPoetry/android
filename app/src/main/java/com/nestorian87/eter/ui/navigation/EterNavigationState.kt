package com.nestorian87.eter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack

@Stable
class EterNavigationState(
    initialAuthenticated: Boolean,
    val authBackStack: NavBackStack<NavKey>,
    val mainBackStacks: Map<TopLevelDestination, NavBackStack<NavKey>>,
) {
    var isAuthenticated by mutableStateOf(initialAuthenticated)
        private set

    var currentTab by mutableStateOf(TopLevelDestination.FEED)
        private set

    val currentTopLevelKey: TopLevelNavKey
        get() = currentTab.navKey

    val currentBackStack: NavBackStack<NavKey>
        get() = if (isAuthenticated) {
            mainBackStacks.getValue(currentTab)
        } else {
            authBackStack
        }

    fun selectTab(destination: TopLevelDestination) {
        currentTab = destination
        val stack = mainBackStacks.getValue(destination)
        if (stack.isEmpty()) {
            stack.add(destination.navKey)
        }
    }

    fun navigate(key: EterNavKey) {
        currentBackStack.add(key)
    }

    fun pop(): Boolean {
        val stack = currentBackStack
        return if (stack.size > 1) {
            stack.removeLastOrNull()
            true
        } else {
            false
        }
    }

    fun showAuth() {
        isAuthenticated = false
        if (authBackStack.isEmpty()) {
            authBackStack.add(LoginKey)
        }
    }

    fun showMain() {
        isAuthenticated = true
        selectTab(TopLevelDestination.FEED)
    }
}

@Composable
fun rememberEterNavigationState(
    initialAuthenticated: Boolean = false,
): EterNavigationState {
    val authBackStack = rememberNavBackStack(LoginKey)
    val feedBackStack = rememberNavBackStack(FeedKey)
    val subscriptionsBackStack = rememberNavBackStack(SubscriptionsKey)
    val createBackStack = rememberNavBackStack(CreateKey)
    val favoritesBackStack = rememberNavBackStack(FavoritesKey)
    val profileBackStack = rememberNavBackStack(ProfileKey)

    return remember(
        authBackStack,
        feedBackStack,
        subscriptionsBackStack,
        createBackStack,
        favoritesBackStack,
        profileBackStack,
        initialAuthenticated,
    ) {
        EterNavigationState(
            initialAuthenticated = initialAuthenticated,
            authBackStack = authBackStack,
            mainBackStacks = mapOf(
                TopLevelDestination.FEED to feedBackStack,
                TopLevelDestination.SUBSCRIPTIONS to subscriptionsBackStack,
                TopLevelDestination.CREATE to createBackStack,
                TopLevelDestination.FAVORITES to favoritesBackStack,
                TopLevelDestination.PROFILE to profileBackStack,
            ),
        )
    }
}
