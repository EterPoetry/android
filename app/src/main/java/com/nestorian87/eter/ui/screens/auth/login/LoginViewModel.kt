package com.nestorian87.eter.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.screens.auth.AuthSubmissionType
import com.nestorian87.eter.ui.screens.auth.AuthUiMessage
import com.nestorian87.eter.ui.screens.auth.isValidEmail
import com.nestorian87.eter.ui.screens.auth.toAuthUiMessage
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<LoginEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                email = value,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                password = value,
                errorMessage = null,
            )
        }
    }

    fun onPasswordVisibilityToggled() {
        _uiState.update { current ->
            current.copy(isPasswordVisible = !current.isPasswordVisible)
        }
    }

    fun onGoogleAuthFailed() {
        _uiState.update { current ->
            current.copy(
                activeSubmission = null,
                errorMessage = AuthUiMessage.GoogleAuthFailed,
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
                    errorMessage = null,
                )
            }
            runCatching {
                authRepository.loginWithGoogle(idToken = idToken)
            }.onSuccess {
                _uiState.update { it.copy(activeSubmission = null, errorMessage = null) }
                effectChannel.send(LoginEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        activeSubmission = null,
                        errorMessage = error.toAuthUiMessage(),
                    )
                }
            }
        }
    }

    fun onLoginClick() {
        val snapshot = _uiState.value
        if (snapshot.activeSubmission != null) {
            return
        }

        val email = snapshot.email.trim()
        val password = snapshot.password
        val errorMessage = when {
            email.isBlank() || password.isBlank() ->
                AuthUiMessage.Validation.FILL_CREDENTIALS
            !isValidEmail(email) -> AuthUiMessage.Validation.INVALID_EMAIL
            password.length < AuthInputLimits.MIN_PASSWORD_LENGTH ->
                AuthUiMessage.Validation.PASSWORD_TOO_SHORT
            else -> null
        }
        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeSubmission = AuthSubmissionType.CREDENTIALS,
                    errorMessage = null,
                )
            }
            runCatching {
                authRepository.login(email = email, password = password)
            }.onSuccess {
                _uiState.update { it.copy(activeSubmission = null, errorMessage = null) }
                effectChannel.send(LoginEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        activeSubmission = null,
                        errorMessage = error.toAuthUiMessage(),
                    )
                }
            }
        }
    }
}
