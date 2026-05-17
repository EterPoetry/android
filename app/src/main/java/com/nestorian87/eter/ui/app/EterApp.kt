package com.nestorian87.eter.ui.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.ui.components.PostPlayerViewModel
import com.nestorian87.eter.ui.components.MiniPlayer
import com.nestorian87.eter.ui.navigation.BottomNavBar
import com.nestorian87.eter.ui.navigation.EterDeepLink
import com.nestorian87.eter.ui.navigation.EterNavigation
import com.nestorian87.eter.ui.navigation.TopLevelDestination
import com.nestorian87.eter.ui.navigation.rememberEterNavigationState
import com.nestorian87.eter.ui.screens.auth.emailConfirmation.EmailConfirmationScreen
import kotlinx.coroutines.delay

@Composable
fun EterApp(
    viewModel: AppSessionViewModel = hiltViewModel(),
    externalIntent: Intent? = null,
    onExternalIntentConsumed: (Intent) -> Unit = {},
) {
    val sessionState = viewModel.uiState.collectAsStateWithLifecycle().value
    val postPlayerViewModel: PostPlayerViewModel = hiltViewModel()
    if (sessionState.isCheckingSession) {
        return
    }

    val navigationState = rememberEterNavigationState(
        initialAuthenticated = sessionState.isAuthenticated,
    )

    LaunchedEffect(sessionState.isAuthenticated, sessionState.isCheckingSession) {
        if (sessionState.isAuthenticated) {
            navigationState.showMain()
        } else {
            navigationState.showAuth()
        }
    }

    LaunchedEffect(externalIntent, sessionState.isAuthenticated) {
        val intent = externalIntent ?: return@LaunchedEffect
        val postRequest = EterDeepLink.parsePostRequest(intent)
        if (postRequest != null && sessionState.isAuthenticated) {
            navigationState.openPostFromExternal(
                postId = postRequest.postId,
                focusComments = postRequest.focusComments,
            )
        }
        onExternalIntentConsumed(intent)
    }

    if (sessionState.isAuthenticated && !sessionState.isEmailVerified) {
        EmailConfirmationScreen()
        return
    }

    NotificationPermissionEffect(
        shouldRequest = sessionState.isAuthenticated,
    )

    val isCreateFlowActive = navigationState.isAuthenticated &&
        navigationState.currentTab == TopLevelDestination.CREATE
    var isMiniPlayerVisible by remember { mutableStateOf(!isCreateFlowActive) }

    LaunchedEffect(isCreateFlowActive) {
        if (isCreateFlowActive) {
            isMiniPlayerVisible = false
            postPlayerViewModel.pausePlayback()
        } else {
            delay(MINI_PLAYER_REAPPEAR_DELAY_MS)
            isMiniPlayerVisible = true
        }
    }

    Scaffold(
        bottomBar = {
            if (navigationState.isAuthenticated) {
                Column {
                    if (isMiniPlayerVisible) {
                        MiniPlayer(
                            onOpenPost = { postId ->
                                navigationState.navigate(com.nestorian87.eter.ui.navigation.PostKey(postId = postId))
                            },
                            onOpenComments = { postId ->
                                navigationState.navigate(
                                    com.nestorian87.eter.ui.navigation.PostKey(
                                        postId = postId,
                                        focusComments = true,
                                    ),
                                )
                            },
                        )
                    }
                    BottomNavBar(
                        currentTab = navigationState.selectedTab,
                        onTabSelected = navigationState::selectTab,
                    )
                }
            }
        }
    ) { innerPadding ->
        EterNavigation(
            navigationState = navigationState,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun NotificationPermissionEffect(
    shouldRequest: Boolean,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return
    }
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(NOTIFICATION_PERMISSION_PREFS, android.content.Context.MODE_PRIVATE)
    }
    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            preferences.edit { putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true) }
        }
    }

    LaunchedEffect(shouldRequest) {
        if (!shouldRequest) {
            return@LaunchedEffect
        }
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            preferences.edit { putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true) }
            return@LaunchedEffect
        }
        val alreadyRequested = preferences.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)
        if (alreadyRequested) {
            return@LaunchedEffect
        }
        preferences.edit { putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true) }
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private const val NOTIFICATION_PERMISSION_PREFS = "eter_permissions"
private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
private const val MINI_PLAYER_REAPPEAR_DELAY_MS = 220L
