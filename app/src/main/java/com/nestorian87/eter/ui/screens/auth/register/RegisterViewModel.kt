package com.nestorian87.eter.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
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
                errorMessage = null,
            )
        }
    }

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

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                errorMessage = null,
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

    fun onRegisterClick() {
        val snapshot = _uiState.value
        if (snapshot.isSubmitting) {
            return
        }

        val name = snapshot.name.trim()
        val email = snapshot.email.trim()
        val password = snapshot.password
        val confirmPassword = snapshot.confirmPassword

        val errorMessage = when {
            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                AuthUiMessage.Validation.REGISTER_FILL_FIELDS
            !isValidEmail(email) -> AuthUiMessage.Validation.INVALID_EMAIL
            password.length < AuthInputLimits.MIN_PASSWORD_LENGTH ->
                AuthUiMessage.Validation.PASSWORD_TOO_SHORT
            password != confirmPassword ->
                AuthUiMessage.Validation.REGISTER_PASSWORD_MISMATCH
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                authRepository.register(
                    name = name,
                    email = email,
                    password = password,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = null) }
                effectChannel.send(RegisterEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toAuthUiMessage(),
                    )
                }
            }
        }
    }
}
