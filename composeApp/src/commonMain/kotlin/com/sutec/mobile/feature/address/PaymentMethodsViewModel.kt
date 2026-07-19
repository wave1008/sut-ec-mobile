package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.StateFlow

class PaymentMethodsViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val paymentMethods: StateFlow<List<PaymentMethod>> = accountRepository.paymentMethods

    fun delete(id: String) = accountRepository.deletePayment(id)

    fun setDefault(id: String) = accountRepository.setDefaultPayment(id)
}
