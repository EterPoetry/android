package com.nestorian87.eter.ui.screens.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.R
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.repository.AuthRepository
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

    fun onPasswordVisibilityToggled() {
        _uiState.update { current ->
            current.copy(isPasswordVisible = !current.isPasswordVisible)
        }
    }

    fun onLoginClick() {
        val snapshot = _uiState.value
        if (snapshot.isSubmitting) return

        val email = snapshot.email.trim()
        val password = snapshot.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessageResId = R.string.auth_fill_credentials_error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessageResId = null) }
            runCatching {
                authRepository.login(email = email, password = password)
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, errorMessageResId = null) }
                effectChannel.send(LoginEffect.NavigateToMain)
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
        AuthException.Reason.NETWORK -> R.string.auth_network_error
        AuthException.Reason.UNKNOWN -> R.string.auth_unexpected_error
    }

    else -> R.string.auth_unexpected_error
}
