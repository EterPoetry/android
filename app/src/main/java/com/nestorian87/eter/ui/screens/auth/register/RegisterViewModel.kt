package com.nestorian87.eter.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.FieldViolation
import com.nestorian87.eter.domain.model.ServerValidationException
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.screens.auth.AuthSubmissionType
import com.nestorian87.eter.ui.screens.auth.AuthUiMessage
import com.nestorian87.eter.ui.screens.auth.isValidEmail
import com.nestorian87.eter.ui.screens.auth.toAuthUiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<RegisterEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onNameChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                name = value,
                formErrorMessage = null,
            )
        }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                username = value,
                usernameErrorMessage = null,
                formErrorMessage = null,
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                email = value,
                emailErrorMessage = null,
                formErrorMessage = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                password = value,
                formErrorMessage = null,
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                formErrorMessage = null,
            )
        }
    }

    fun onPasswordVisibilityToggled() {
        _uiState.update { current ->
            current.copy(isPasswordVisible = !current.isPasswordVisible)
        }
    }

    fun onConfirmPasswordVisibilityToggled() {
        _uiState.update { current ->
            current.copy(isConfirmPasswordVisible = !current.isConfirmPasswordVisible)
        }
    }

    fun onGoogleAuthFailed() {
        _uiState.update { current ->
            current.copy(
                activeSubmission = null,
                formErrorMessage = AuthUiMessage.GoogleAuthFailed,
            )
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        if (_uiState.value.activeSubmission != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeSubmission = AuthSubmissionType.GOOGLE,
                    formErrorMessage = null,
                )
            }
            runCatching {
                authRepository.loginWithGoogle(idToken = idToken)
            }.onSuccess {
                _uiState.update { it.copy(activeSubmission = null, formErrorMessage = null) }
                effectChannel.send(RegisterEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        activeSubmission = null,
                        formErrorMessage = error.toAuthUiMessage(),
                    )
                }
            }
        }
    }

    fun onRegisterClick() {
        val snapshot = _uiState.value
        if (snapshot.activeSubmission != null) {
            return
        }

        val name = snapshot.name.trim()
        val username = snapshot.username.trim()
        val email = snapshot.email.trim()
        val password = snapshot.password
        val confirmPassword = snapshot.confirmPassword

        val formErrorMessage = when {
            name.isBlank() ||
                username.isBlank() ||
                email.isBlank() ||
                password.isBlank() ||
                confirmPassword.isBlank() ->
                AuthUiMessage.Validation.REGISTER_FILL_FIELDS
            !isValidEmail(email) -> AuthUiMessage.Validation.INVALID_EMAIL
            password.length < AuthInputLimits.MIN_PASSWORD_LENGTH ->
                AuthUiMessage.Validation.PASSWORD_TOO_SHORT
            password != confirmPassword ->
                AuthUiMessage.Validation.REGISTER_PASSWORD_MISMATCH
            else -> null
        }

        if (formErrorMessage != null) {
            _uiState.update {
                it.copy(
                    usernameErrorMessage = null,
                    emailErrorMessage = null,
                    formErrorMessage = formErrorMessage,
                )
            }
            return
        }

        if (username.length < AuthInputLimits.MIN_USERNAME_LENGTH) {
            _uiState.update {
                it.copy(
                    usernameErrorMessage = AuthUiMessage.Validation.USERNAME_TOO_SHORT,
                    emailErrorMessage = null,
                    formErrorMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeSubmission = AuthSubmissionType.CREDENTIALS,
                    usernameErrorMessage = null,
                    emailErrorMessage = null,
                    formErrorMessage = null,
                )
            }
            runCatching {
                authRepository.register(
                    name = name,
                    username = username,
                    email = email,
                    password = password,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        activeSubmission = null,
                        usernameErrorMessage = null,
                        emailErrorMessage = null,
                        formErrorMessage = null,
                    )
                }
                effectChannel.send(RegisterEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update { current ->
                    current.withSubmissionError(error)
                }
            }
        }
    }

    private fun RegisterUiState.withSubmissionError(error: Throwable): RegisterUiState {
        val validationError = error as? ServerValidationException
        val fieldViolations = validationError?.fieldViolations.orEmpty()

        val usernameErrorMessage = fieldViolations
            .toFieldErrorMessage(
                field = RegistrationField.USERNAME,
                code = RegistrationErrorCode.USERNAME_NOT_UNIQUE,
                resId = R.string.auth_register_username_exists_error,
            )
        val emailErrorMessage = fieldViolations
            .toFieldErrorMessage(
                field = RegistrationField.EMAIL,
                code = RegistrationErrorCode.EMAIL_NOT_UNIQUE,
                resId = R.string.auth_register_email_exists_error,
            )
        val formErrorMessage = if (usernameErrorMessage != null || emailErrorMessage != null) {
            null
        } else {
            error.toAuthUiMessage()
        }

        return copy(
            activeSubmission = null,
            usernameErrorMessage = usernameErrorMessage,
            emailErrorMessage = emailErrorMessage,
            formErrorMessage = formErrorMessage,
        )
    }

    private fun Set<FieldViolation>.toFieldErrorMessage(
        field: String,
        code: String,
        resId: Int,
    ): AuthUiMessage? {
        val hasViolation = any { violation ->
            violation.field == field && violation.code == code
        }

        return if (hasViolation) {
            AuthUiMessage.ResourceMessage(resId)
        } else {
            null
        }
    }

    private object RegistrationField {
        const val EMAIL = "email"
        const val USERNAME = "username"
    }

    private object RegistrationErrorCode {
        const val EMAIL_NOT_UNIQUE = "EMAIL_NOT_UNIQUE"
        const val USERNAME_NOT_UNIQUE = "USERNAME_NOT_UNIQUE"
    }
}
