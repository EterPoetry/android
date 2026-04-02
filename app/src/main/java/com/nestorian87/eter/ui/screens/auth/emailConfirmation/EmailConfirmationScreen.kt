package com.nestorian87.eter.ui.screens.auth.emailConfirmation

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.LoadingOverlay
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.TextAction
import com.nestorian87.eter.ui.components.VerificationCodeField
import com.nestorian87.eter.ui.screens.auth.toMessageResId
import com.nestorian87.eter.ui.screens.auth.emailConfirmation.EmailConfirmationUiState
import com.nestorian87.eter.ui.screens.auth.emailConfirmation.EmailConfirmationViewModel
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun EmailConfirmationScreen(
    modifier: Modifier = Modifier,
    viewModel: EmailConfirmationViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    EmailConfirmationScreenContent(
        uiState = uiState,
        modifier = modifier.fillMaxSize(),
        onCodeChanged = viewModel::onCodeChanged,
        onRequestCodeClick = viewModel::onRequestCodeClick,
        onLogoutClick = viewModel::onLogoutClick,
    )
}

@Composable
private fun EmailConfirmationScreenContent(
    uiState: EmailConfirmationUiState,
    onCodeChanged: (String) -> Unit,
    onRequestCodeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomBarContentInset = 164.dp
    val showSentDescription = uiState.isInitializing ||
        uiState.isRequestingCode ||
        uiState.hasRequestedCode ||
        uiState.remainingMs != null
    val canEnterVerificationCode = uiState.hasRequestedCode || uiState.remainingMs != null
    val descriptionTemplate = if (showSentDescription) {
        stringResource(R.string.auth_email_verification_sent_description, uiState.email)
    } else {
        stringResource(R.string.auth_email_verification_description, uiState.email)
    }
    val descriptionText = buildAnnotatedString {
        append(descriptionTemplate)
        if (uiState.email.isNotBlank()) {
            val startIndex = descriptionTemplate.indexOf(uiState.email)
            if (startIndex >= 0) {
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    start = startIndex,
                    end = startIndex + uiState.email.length,
                )
            }
        }
    }
    val buttonText = when {
        uiState.remainingMs != null -> stringResource(
            R.string.auth_email_verification_resend_in,
            uiState.remainingMs.toTimerText(),
        )
        uiState.hasRequestedCode -> stringResource(R.string.auth_email_verification_resend_code)
        else -> stringResource(R.string.auth_email_verification_send_code)
    }

    Box(modifier = modifier) {
        FormScreenScaffold(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = EterSpacing.xxxLarge,
                top = EterSpacing.xxLarge,
                end = EterSpacing.xxxLarge,
                bottom = bottomBarContentInset,
            ),
            bottomBar = {
                PrimaryActionButton(
                    text = buttonText,
                    enabled = uiState.remainingMs == null,
                    isLoading = uiState.isRequestingCode && !uiState.hasRequestedCode,
                    onClick = onRequestCodeClick,
                )
            },
        ) {
            Spacer(modifier = Modifier.height(EterSpacing.screen))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = stringResource(R.string.brand_logo_content_description),
                    modifier = Modifier.size(60.dp),
                )
            }
            Spacer(modifier = Modifier.height(EterSpacing.hero))
            Text(
                text = stringResource(R.string.auth_email_verification_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(EterSpacing.screen))
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(48.dp))
            AnimatedVisibility(
                visible = canEnterVerificationCode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                VerificationCodeField(
                    value = uiState.code,
                    onValueChange = onCodeChanged,
                    enabled = !uiState.isInitializing,
                    readOnly = uiState.isVerifyingCode,
                )
            }
            AnimatedErrorMessage(messageResId = uiState.errorMessage?.toMessageResId())
            Spacer(modifier = Modifier.height(EterSpacing.hero))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                TextAction(
                    text = stringResource(R.string.auth_email_verification_logout_cta),
                    onClick = onLogoutClick,
                )
            }
            Spacer(modifier = Modifier.height(EterSpacing.xxLarge))
        }

        LoadingOverlay(
            isVisible = uiState.isInitializing,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun Long.toTimerText(): String {
    val totalSeconds = (this / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

@EterScreenPreviews
@Composable
private fun EmailConfirmationScreenPreview() {
    EterPreview {
        EmailConfirmationScreenContent(
            uiState = EmailConfirmationUiState(
                email = "example@gmail.com",
                hasRequestedCode = true,
                remainingMs = 596_000L,
            ),
            onCodeChanged = {},
            onRequestCodeClick = {},
            onLogoutClick = {},
        )
    }
}
