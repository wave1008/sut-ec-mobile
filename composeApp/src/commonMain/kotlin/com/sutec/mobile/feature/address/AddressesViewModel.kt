package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.StateFlow

class AddressesViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val addresses: StateFlow<List<Address>> = accountRepository.addresses

    fun delete(id: String) = accountRepository.deleteAddress(id)

    fun setDefault(id: String) = accountRepository.setDefaultAddress(id)
}
