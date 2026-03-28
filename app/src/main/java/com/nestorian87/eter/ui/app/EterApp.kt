package com.nestorian87.eter.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nestorian87.eter.ui.components.MiniPlayer
import com.nestorian87.eter.ui.navigation.BottomNavBar
import com.nestorian87.eter.ui.navigation.EterNavigation
import com.nestorian87.eter.ui.navigation.rememberEterNavigationState

@Composable
fun EterApp() {
    val navigationState = rememberEterNavigationState()

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
