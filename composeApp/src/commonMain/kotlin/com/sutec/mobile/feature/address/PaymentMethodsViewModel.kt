package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.repository.AccountRepository
import com.sutec.mobile.data.repository.UnauthorizedException
import com.sutec.mobile.util.AppMessages
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentMethodsViewModel(
    private val accountRepository: AccountRepository,
    private val appMessages: AppMessages,
) : ViewModel() {

    val paymentMethods: StateFlow<List<PaymentMethod>> = accountRepository.paymentMethods

    fun delete(id: String) {
        viewModelScope.launch {
            accountRepository.deletePayment(id).onFailure { reportFailure(it) }
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch {
            accountRepository.setDefaultPayment(id).onFailure { reportFailure(it) }
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
