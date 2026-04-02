package com.nestorian87.eter.ui.screens.auth.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import com.nestorian87.eter.R
import com.nestorian87.eter.ui.components.FormScreenScaffold
import com.nestorian87.eter.ui.components.GoogleAuthButton
import com.nestorian87.eter.ui.components.LabeledDivider
import com.nestorian87.eter.ui.components.PrimaryActionButton
import com.nestorian87.eter.ui.components.TextAction
import com.nestorian87.eter.ui.components.UnderlinedTextField
import com.nestorian87.eter.ui.components.AnimatedErrorMessage
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.theme.EterPreview
import com.nestorian87.eter.ui.theme.EterScreenPreviews
import com.nestorian87.eter.ui.theme.EterSpacing

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(onRegisterSuccess) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToMain -> onRegisterSuccess()
            }
        }
    }

    RegisterScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onPasswordVisibilityToggled = viewModel::onPasswordVisibilityToggled,
        onConfirmPasswordVisibilityToggled = viewModel::onConfirmPasswordVisibilityToggled,
        onRegisterClick = viewModel::onRegisterClick,
        onNavigateToLogin = onNavigateToLogin,
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: RegisterUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onPasswordVisibilityToggled: () -> Unit,
    onConfirmPasswordVisibilityToggled: () -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val bottomBarContentInset = 148.dp

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
                text = stringResource(R.string.auth_register_cta),
                enabled = uiState.name.isNotBlank() &&
                    uiState.email.isNotBlank() &&
                    uiState.password.isNotBlank() &&
                    uiState.confirmPassword.isNotBlank(),
                isLoading = uiState.isSubmitting,
                onClick = onRegisterClick,
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
            value = uiState.name,
            onValueChange = onNameChanged,
            label = stringResource(R.string.auth_name_label),
            placeholder = stringResource(R.string.auth_name_placeholder),
            maxLength = AuthInputLimits.MAX_NAME_LENGTH,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { emailFocusRequester.requestFocus() },
            ),
        )
        Spacer(modifier = Modifier.height(EterSpacing.section))
        UnderlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            label = stringResource(R.string.auth_email_label),
            placeholder = stringResource(R.string.auth_email_placeholder),
            maxLength = AuthInputLimits.MAX_EMAIL_LENGTH,
            modifier = Modifier.focusRequester(emailFocusRequester),
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
                PasswordVisibilityIcon(
                    isVisible = uiState.isPasswordVisible,
                    onClick = onPasswordVisibilityToggled,
                )
            },
            visualTransformation = if (uiState.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { confirmPasswordFocusRequester.requestFocus() },
            ),
        )
        Spacer(modifier = Modifier.height(EterSpacing.section))
        UnderlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChanged,
            label = stringResource(R.string.auth_confirm_password_label),
            placeholder = stringResource(R.string.auth_confirm_password_placeholder),
            maxLength = AuthInputLimits.MAX_PASSWORD_LENGTH,
            modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
            trailing = {
                PasswordVisibilityIcon(
                    isVisible = uiState.isConfirmPasswordVisible,
                    onClick = onConfirmPasswordVisibilityToggled,
                )
            },
            visualTransformation = if (uiState.isConfirmPasswordVisible) {
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
                    onRegisterClick()
                },
            ),
        )
        AnimatedErrorMessage(messageResId = uiState.errorMessageResId)
        Spacer(modifier = Modifier.height(EterSpacing.xLarge))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.auth_existing_account_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(EterSpacing.xSmall))
            TextAction(
                text = stringResource(R.string.auth_login_short_cta),
                onClick = onNavigateToLogin,
            )
        }
        Spacer(modifier = Modifier.height(EterSpacing.screen))
        LabeledDivider(text = stringResource(R.string.auth_or))
        Spacer(modifier = Modifier.height(EterSpacing.xxxLarge))
        GoogleAuthButton(
            text = stringResource(R.string.auth_google_cta),
        )
        Spacer(modifier = Modifier.height(EterSpacing.xxLarge))
    }
}

@Composable
private fun PasswordVisibilityIcon(
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = if (isVisible) {
            Icons.Outlined.Visibility
        } else {
            Icons.Outlined.VisibilityOff
        },
        contentDescription = stringResource(R.string.auth_toggle_password_visibility),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .size(22.dp)
            .clickable(onClick = onClick),
    )
}

@EterScreenPreviews
@Composable
private fun RegisterScreenPreview() {
    EterPreview {
        RegisterScreenContent(
            uiState = RegisterUiState(),
            onNameChanged = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            onPasswordVisibilityToggled = {},
            onConfirmPasswordVisibilityToggled = {},
            onRegisterClick = {},
            onNavigateToLogin = {},
        )
    }
}
