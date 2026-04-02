package com.nestorian87.eter.ui.screens.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestorian87.eter.BuildConfig
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.GoogleAuthButton
import com.nestorian87.eter.ui.components.LabeledDivider
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.TextAction
import com.nestorian87.eter.ui.components.UnderlinedTextField
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.screens.auth.AuthSubmissionType
import com.nestorian87.eter.ui.screens.auth.toMessageResId
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.screens.auth.google.rememberGoogleIdTokenRequester
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(onLoginSuccess) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToMain -> onLoginSuccess()
            }
        }
    }

    LoginScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onPasswordVisibilityToggled = viewModel::onPasswordVisibilityToggled,
        onLoginClick = viewModel::onLoginClick,
        onGoogleIdTokenReceived = viewModel::onGoogleIdTokenReceived,
        onGoogleAuthFailed = viewModel::onGoogleAuthFailed,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
    )
}

@Composable
private fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggled: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleIdTokenReceived: (String) -> Unit,
    onGoogleAuthFailed: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val bottomBarContentInset = 148.dp
    val isSubmitting = uiState.activeSubmission != null
    val requestGoogleIdToken = rememberGoogleIdTokenRequester(
        serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
        onTokenReceived = onGoogleIdTokenReceived,
        onFailure = onGoogleAuthFailed,
    )

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
                text = stringResource(R.string.auth_login_cta),
                enabled = !isSubmitting && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                isLoading = uiState.activeSubmission == AuthSubmissionType.CREDENTIALS,
                onClick = onLoginClick,
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
        UnderlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder),
            maxLength = AuthInputLimits.MAX_EMAIL_LENGTH,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() },
            ),
        )
        Spacer(modifier = Modifier.height(EterSpacing.section))
        UnderlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = stringResource(R.string.auth_password_label),
            placeholder = stringResource(R.string.auth_password_placeholder),
            maxLength = AuthInputLimits.MAX_PASSWORD_LENGTH,
            modifier = Modifier.focusRequester(passwordFocusRequester),
            trailing = {
                Icon(
                    imageVector = if (uiState.isPasswordVisible) {
                        Icons.Outlined.Visibility
                    } else {
                        Icons.Outlined.VisibilityOff
                    },
                    contentDescription = stringResource(R.string.auth_toggle_password_visibility),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onPasswordVisibilityToggled),
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClick()
                },
            ),
        )
        AnimatedErrorMessage(messageResId = uiState.errorMessage?.toMessageResId())
        Spacer(modifier = Modifier.height(EterSpacing.large))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextAction(
                text = stringResource(R.string.auth_forgot_password_cta),
                onClick = onNavigateToForgotPassword,
            )
            TextAction(
                text = stringResource(R.string.auth_create_account_cta),
                onClick = onNavigateToRegister,
            )
        }
        Spacer(modifier = Modifier.height(EterSpacing.screen))
        LabeledDivider(text = stringResource(R.string.auth_or))
        Spacer(modifier = Modifier.height(EterSpacing.xxxLarge))
        GoogleAuthButton(
            text = stringResource(R.string.auth_google_cta),
            enabled = !isSubmitting,
            isLoading = uiState.activeSubmission == AuthSubmissionType.GOOGLE,
            onClick = requestGoogleIdToken,
        )
        Spacer(modifier = Modifier.height(EterSpacing.xxLarge))
    }
}

@EterScreenPreviews
@Composable
private fun LoginScreenPreview() {
    EterPreview {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChanged = {},
            onPasswordChanged = {},
            onPasswordVisibilityToggled = {},
            onLoginClick = {},
            onGoogleIdTokenReceived = {},
            onGoogleAuthFailed = {},
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {},
        )
    }
}
