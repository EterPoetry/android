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

    var pendingSearchCategoryId: Long? by mutableStateOf(null)
        private set
    private var searchReturnTab: TopLevelDestination? by mutableStateOf(null)

    val currentBackStack: NavBackStack<NavKey>
        get() = if (isAuthenticated) {
            mainBackStacks.getValue(currentTab)
        } else {
            authBackStack
        }

    val selectedTab: TopLevelDestination?
        get() = when (currentBackStack.lastOrNull()) {
            is PostKey -> null
            is TopLevelNavKey -> currentTab
            else -> currentTab
        }

    fun selectTab(destination: TopLevelDestination) {
        val previousTab = currentTab
        if (destination != TopLevelDestination.SEARCH) {
            searchReturnTab = null
        }
        currentTab = destination
        val stack = mainBackStacks.getValue(destination)
        if (destination == TopLevelDestination.CREATE && stack.any { it !is CreateKey }) {
            resetCreateStack(stack)
            return
        }
        if (previousTab == destination && stack.size > 1) {
            while (stack.size > 1) {
                stack.removeLastOrNull()
            }
            return
        }
        if (stack.isEmpty()) {
            stack.add(rootKeyFor(destination))
        }
    }

    fun openSearchWithCategory(categoryId: Long) {
        pendingSearchCategoryId = categoryId
        openSearch()
    }

    fun openSearch() {
        if (currentTab != TopLevelDestination.SEARCH) {
            searchReturnTab = currentTab
        }
        selectTab(TopLevelDestination.SEARCH)
    }

    fun clearPendingSearchCategory() {
        pendingSearchCategoryId = null
    }

    fun navigate(key: EterNavKey) {
        currentBackStack.add(key)
    }

    fun openPostFromExternal(
        postId: Long,
        focusComments: Boolean = false,
    ) {
        isAuthenticated = true
        currentTab = TopLevelDestination.FEED
        val stack = mainBackStacks.getValue(TopLevelDestination.FEED)
        while (stack.size > 1) {
            stack.removeLastOrNull()
        }
        val nextPostKey = PostKey(
            postId = postId,
            focusComments = focusComments,
        )
        val currentTop = stack.lastOrNull()
        if (currentTop is PostKey && currentTop.postId == postId && currentTop.focusComments == focusComments) {
            stack.removeLastOrNull()
        }
        stack.add(nextPostKey)
    }

    fun pop(): Boolean {
        val stack = currentBackStack
        return if (stack.size > 1) {
            stack.removeLastOrNull()
            true
        } else if (isAuthenticated && currentTab == TopLevelDestination.SEARCH) {
            val returnTab = searchReturnTab ?: TopLevelDestination.FEED
            pendingSearchCategoryId = null
            searchReturnTab = null
            selectTab(returnTab)
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

    private fun resetCreateStack(stack: NavBackStack<NavKey>) {
        while (stack.isNotEmpty()) {
            stack.removeLastOrNull()
        }
        stack.add(CreateKey())
    }

    private fun rootKeyFor(destination: TopLevelDestination): TopLevelNavKey = when (destination) {
        TopLevelDestination.CREATE -> CreateKey()
        else -> destination.navKey
    }
}

@Composable
fun rememberEterNavigationState(
    initialAuthenticated: Boolean = false,
): EterNavigationState {
    val authBackStack = rememberNavBackStack(LoginKey)
    val feedBackStack = rememberNavBackStack(FeedKey)
    val searchBackStack = rememberNavBackStack(SearchKey)
    val subscriptionsBackStack = rememberNavBackStack(SubscriptionsKey)
    val createBackStack = rememberNavBackStack(CreateKey())
    val favoritesBackStack = rememberNavBackStack(FavoritesKey)
    val profileBackStack = rememberNavBackStack(ProfileKey)

    return remember(
        authBackStack,
        feedBackStack,
        searchBackStack,
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
                TopLevelDestination.SEARCH to searchBackStack,
                TopLevelDestination.SUBSCRIPTIONS to subscriptionsBackStack,
                TopLevelDestination.CREATE to createBackStack,
                TopLevelDestination.FAVORITES to favoritesBackStack,
                TopLevelDestination.PROFILE to profileBackStack,
            ),
        )
    }
}
