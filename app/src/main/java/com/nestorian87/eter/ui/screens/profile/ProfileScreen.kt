package com.nestorian87.eter.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    ProfileScreenContent(
        uiState = uiState,
        modifier = modifier,
        onLogoutClick = viewModel::onLogoutClick,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = EterSpacing.xxxLarge, vertical = EterSpacing.section),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(horizontal = EterSpacing.section, vertical = EterSpacing.xLarge),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.name.take(1).ifBlank { "E" },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(EterSpacing.screen))
        Text(
            text = uiState.name.ifBlank { stringResource(R.string.profile_default_name) },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(EterSpacing.xSmall))
        Text(
            text = uiState.email.ifBlank { stringResource(R.string.profile_default_email) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(60.dp))
        PrimaryActionButton(
            text = stringResource(R.string.profile_logout_cta),
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isLoggingOut,
            onClick = onLogoutClick,
        )
    }
}

@EterScreenPreviews
@Composable
private fun ProfileScreenPreview() {
    EterPreview {
        ProfileScreenContent(
            uiState = ProfileUiState(
                name = "User",
                email = "user@example.com",
            ),
            onLogoutClick = {},
        )
    }
}
