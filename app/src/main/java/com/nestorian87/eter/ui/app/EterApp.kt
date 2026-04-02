package com.nestorian87.eter.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.ui.components.MiniPlayer
import com.nestorian87.eter.ui.navigation.BottomNavBar
import com.nestorian87.eter.ui.navigation.EterNavigation
import com.nestorian87.eter.ui.navigation.rememberEterNavigationState
import com.nestorian87.eter.ui.screens.auth.emailConfirmation.EmailConfirmationScreen

@Composable
fun EterApp(
    viewModel: AppSessionViewModel = hiltViewModel(),
) {
    val sessionState = viewModel.uiState.collectAsStateWithLifecycle().value
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

    if (sessionState.isAuthenticated && !sessionState.isEmailVerified) {
        EmailConfirmationScreen()
        return
    }

    Scaffold(
        bottomBar = {
            if (navigationState.isAuthenticated) {
                Column {
                    MiniPlayer()
                    BottomNavBar(
                        currentTab = navigationState.currentTab,
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
