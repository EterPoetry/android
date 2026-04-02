package com.nestorian87.eter.ui.screens.auth.emailConfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.model.AuthException
import com.nestorian87.eter.domain.repository.AuthRepository
import com.nestorian87.eter.ui.screens.auth.AuthUiMessage
import com.nestorian87.eter.ui.screens.auth.toAuthUiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EmailConfirmationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmailConfirmationUiState())
    val uiState: StateFlow<EmailConfirmationUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.session.collectLatest { session ->
                val email = session?.user?.email.orEmpty()
                if (email == _uiState.value.email) {
                    return@collectLatest
                }

                countdownJob?.cancel()
                _uiState.value = EmailConfirmationUiState(
                    email = email,
                    isInitializing = email.isNotBlank(),
                )

                if (email.isNotBlank()) {
                    initialize()
                }
            }
        }
    }

    fun onCodeChanged(value: String) {
        val sanitizedValue = value.filter(Char::isDigit).take(EMAIL_VERIFICATION_CODE_LENGTH)
        _uiState.update { current ->
            current.copy(
                code = sanitizedValue,
                errorMessage = null,
            )
        }

        if (sanitizedValue.length == EMAIL_VERIFICATION_CODE_LENGTH) {
            verifyCode()
        }
    }

    fun onRequestCodeClick() {
        val snapshot = _uiState.value
        if (snapshot.isRequestingCode || snapshot.remainingMs != null) {
            return
        }
        requestCode()
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true, errorMessage = null) }
            val remainingMs = runCatching { authRepository.getEmailVerificationStatus() }
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isInitializing = false,
                            errorMessage = error.toAuthUiMessage(),
                        )
                    }
                    return@launch
                }

            if (remainingMs.canRequestCode) {
                requestCode(isInitialRequest = true)
            } else {
                startCountdown(remainingMs.remainingMs)
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        hasRequestedCode = true,
                        remainingMs = remainingMs.remainingMs,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun requestCode(isInitialRequest: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRequestingCode = true,
                    isInitializing = isInitialRequest,
                    errorMessage = null,
                )
            }

            val verificationStatus = runCatching { authRepository.requestEmailVerificationCode() }
                .getOrElse { error ->
                    _uiState.update {
                        it.copy(
                            isRequestingCode = false,
                            isInitializing = false,
                            errorMessage = error.toAuthUiMessage(),
                        )
                    }
                    return@launch
                }

            startCountdown(verificationStatus.remainingMs)
            _uiState.update {
                it.copy(
                    isRequestingCode = false,
                    isInitializing = false,
                    hasRequestedCode = true,
                    remainingMs = verificationStatus.remainingMs,
                    errorMessage = null,
                )
            }
        }
    }

    private fun verifyCode() {
        val snapshot = _uiState.value
        if (snapshot.isVerifyingCode || snapshot.email.isBlank()) {
            return
        }

        if (snapshot.code.length < EMAIL_VERIFICATION_CODE_LENGTH) {
            _uiState.update {
                it.copy(
                    errorMessage = AuthUiMessage.ReasonMessage(
                        reason = com.nestorian87.eter.domain.model.AuthException.Reason.INVALID_VERIFICATION_CODE,
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, errorMessage = null) }
            runCatching {
                authRepository.verifyEmail(
                    email = snapshot.email,
                    code = snapshot.code,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isVerifyingCode = false,
                        code = "",
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isVerifyingCode = false,
                        code = "",
                        errorMessage = error.toAuthUiMessage(),
                    )
                }
            }
        }
    }

    private fun startCountdown(initialRemainingMs: Long?) {
        countdownJob?.cancel()
        if (initialRemainingMs == null || initialRemainingMs <= 0L) {
            _uiState.update { it.copy(remainingMs = null) }
            return
        }
        val startingRemainingMs: Long = initialRemainingMs

        countdownJob = viewModelScope.launch {
            var remainingMs = startingRemainingMs
            while (remainingMs > 0L) {
                _uiState.update { it.copy(remainingMs = remainingMs) }
                delay(1_000L)
                remainingMs = (remainingMs - 1_000L).coerceAtLeast(0L)
            }
            _uiState.update { it.copy(remainingMs = null) }
        }
    }

    private companion object {
        const val EMAIL_VERIFICATION_CODE_LENGTH = 6
    }
}
