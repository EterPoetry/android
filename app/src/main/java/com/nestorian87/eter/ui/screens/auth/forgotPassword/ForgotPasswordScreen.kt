package com.nestorian87.eter.ui.screens.auth.forgotPassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.UnderlinedTextField
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.screens.auth.toMessageResId
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(onCancelClick) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ForgotPasswordEffect.PasswordResetSent -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.auth_forgot_password_sent_snackbar,
                            effect.email,
                        ),
                    )
                    onCancelClick()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ForgotPasswordScreenContent(
            uiState = uiState,
            modifier = Modifier.fillMaxSize(),
            onEmailChanged = viewModel::onEmailChanged,
            onSendInstructionsClick = viewModel::onSendInstructionsClick,
            onCancelClick = onCancelClick,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ForgotPasswordScreenContent(
    uiState: ForgotPasswordUiState,
    onEmailChanged: (String) -> Unit,
    onSendInstructionsClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    val bottomBarContentInset = 104.dp

    FormScreenScaffold(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = EterSpacing.xxxLarge,
            top = EterSpacing.xxLarge,
            end = EterSpacing.xxxLarge,
            bottom = bottomBarContentInset,
        ),
        bottomBar = {
            PrimaryActionButton(
                text = stringResource(R.string.auth_forgot_password_send_cta),
                enabled = uiState.email.isNotBlank(),
                isLoading = uiState.isSubmitting,
                onClick = onSendInstructionsClick,
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.navigation_back),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onCancelClick),
            )
        }
        Spacer(modifier = Modifier.height(EterSpacing.large))
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
            text = stringResource(R.string.auth_forgot_password_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(EterSpacing.screen))
        Text(
            text = stringResource(R.string.auth_forgot_password_description),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(EterSpacing.hero))
        UnderlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder),
            maxLength = AuthInputLimits.MAX_EMAIL_LENGTH,
            modifier = Modifier.focusRequester(emailFocusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSendInstructionsClick()
                },
            ),
        )
        AnimatedErrorMessage(messageResId = uiState.errorMessage?.toMessageResId())
        Spacer(modifier = Modifier.height(EterSpacing.xxLarge))
    }
}

@EterScreenPreviews
@Composable
private fun ForgotPasswordScreenPreview() {
    EterPreview {
        ForgotPasswordScreenContent(
            uiState = ForgotPasswordUiState(),
            onEmailChanged = {},
            onSendInstructionsClick = {},
            onCancelClick = {},
        )
    }
}
