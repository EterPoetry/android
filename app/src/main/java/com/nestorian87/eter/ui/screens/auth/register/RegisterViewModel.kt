package com.nestorian87.eter.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.ui.screens.auth.AuthInputLimits
import com.nestorian87.eter.ui.screens.auth.isValidEmail
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
                errorMessageResId = null,
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                email = value,
                errorMessageResId = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                password = value,
                errorMessageResId = null,
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                confirmPassword = value,
                errorMessageResId = null,
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

        val errorMessageResId = when {
            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                R.string.auth_register_fill_fields_error
            !isValidEmail(email) -> R.string.auth_invalid_email_error
            password.length < AuthInputLimits.MIN_PASSWORD_LENGTH ->
                R.string.auth_password_too_short_error
            password != confirmPassword ->
                R.string.auth_register_password_mismatch_error
            else -> null
        }

        if (errorMessageResId != null) {
            _uiState.update { it.copy(errorMessageResId = errorMessageResId) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessageResId = null) }
            runCatching {
                authRepository.register(
                    name = name,
                    email = email,
                    password = password,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, errorMessageResId = null) }
                effectChannel.send(RegisterEffect.NavigateToMain)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessageResId = error.toUserMessageResId(),
                    )
                }
            }
        }
    }
}

private fun Throwable.toUserMessageResId(): Int = when (this) {
    is AuthException -> when (reason) {
        AuthException.Reason.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials_error
        AuthException.Reason.INVALID_REGISTRATION_DATA ->
            R.string.auth_register_invalid_data_error
        AuthException.Reason.EMAIL_ALREADY_EXISTS -> R.string.auth_register_email_exists_error
        AuthException.Reason.NETWORK -> R.string.auth_network_error
        AuthException.Reason.UNKNOWN -> R.string.auth_unexpected_error
    }

    else -> R.string.auth_unexpected_error
}
