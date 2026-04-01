package com.nestorian87.eter.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nestorian87.eter.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSessionUiState())
    val uiState: StateFlow<AppSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.session.collectLatest { session ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = session != null,
                )
            }
        }

        viewModelScope.launch {
            val restored = authRepository.restoreSession()
            _uiState.value = _uiState.value.copy(
                isCheckingSession = false,
                isAuthenticated = restored || authRepository.session.value != null,
            )
        }
    }
}
