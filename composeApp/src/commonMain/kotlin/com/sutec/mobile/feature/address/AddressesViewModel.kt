package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.repository.AccountRepository
import com.sutec.mobile.data.repository.UnauthorizedException
import com.sutec.mobile.util.AppMessages
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressesViewModel(
    private val accountRepository: AccountRepository,
    private val appMessages: AppMessages,
) : ViewModel() {

    val addresses: StateFlow<List<Address>> = accountRepository.addresses

    fun delete(id: String) {
        viewModelScope.launch {
            accountRepository.deleteAddress(id).onFailure { reportFailure(it) }
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch {
            accountRepository.setDefaultAddress(id).onFailure { reportFailure(it) }
        }
    }

    private fun reportFailure(error: Throwable) {
        if (error is UnauthorizedException) {
            appMessages.show(
                "ログインが必要です。ログインしてから再度お試しください",
                "Please log in to save. Log in and try again.",
            )
        } else {
            appMessages.show(
                "操作に失敗しました。時間をおいて再度お試しください",
                "Operation failed. Please try again.",
            )
        }
    }
}
