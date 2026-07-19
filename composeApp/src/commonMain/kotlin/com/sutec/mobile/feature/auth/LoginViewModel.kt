package com.sutec.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    // 表示文言は Screen 側で tr() に変換する。ここは失敗フラグとして非 null かどうかのみ使う。
    val error: String? = null,
    val success: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(loading = true, error = null)
            authRepository.login(state.email, state.password).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(loading = false, success = true) },
                onFailure = { e -> _uiState.value = _uiState.value.copy(loading = false, error = e.message ?: "login_failed") },
            )
        }
    }
}
