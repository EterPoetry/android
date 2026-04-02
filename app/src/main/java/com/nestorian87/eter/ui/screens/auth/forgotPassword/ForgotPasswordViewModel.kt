package com.nestorian87.eter.ui.screens.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.repository.AuthRepository
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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<ForgotPasswordEffect>(capacity = Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { current ->
            current.copy(
                email = value,
                errorMessage = null,
            )
        }
    }

    fun onSendInstructionsClick() {
        val snapshot = _uiState.value
        if (snapshot.isSubmitting) {
            return
        }

        val email = snapshot.email.trim()
        val errorMessage = when {
            email.isBlank() -> AuthUiMessage.Validation.FORGOT_PASSWORD_FILL_FIELD
            !isValidEmail(email) -> AuthUiMessage.Validation.INVALID_EMAIL
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                authRepository.requestPasswordReset(email = email)
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = null) }
                effectChannel.send(ForgotPasswordEffect.PasswordResetSent(email = email))
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
