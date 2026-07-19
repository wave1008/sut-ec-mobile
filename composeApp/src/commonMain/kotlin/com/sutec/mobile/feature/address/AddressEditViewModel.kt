package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AddressEditUiState(
    val addressId: String? = null,
    val fullName: String = "",
    val postalCode: String = "",
    val prefecture: String = "",
    val city: String = "",
    val line1: String = "",
    val line2: String = "",
    val phone: String = "",
    val isDefault: Boolean = false,
    val saved: Boolean = false,
)

class AddressEditViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressEditUiState())
    val uiState: StateFlow<AddressEditUiState> = _uiState.asStateFlow()

    fun load(addressId: String?) {
        val existing = addressId?.let { accountRepository.getAddress(it) }
        _uiState.value = if (existing != null) {
            AddressEditUiState(
                addressId = existing.id,
                fullName = existing.fullName,
                postalCode = existing.postalCode,
                prefecture = existing.prefecture,
                city = existing.city,
                line1 = existing.line1,
                line2 = existing.line2,
                phone = existing.phone,
                isDefault = existing.isDefault,
            )
        } else {
            AddressEditUiState()
        }
    }

    fun onFullNameChange(value: String) { _uiState.value = _uiState.value.copy(fullName = value) }
    fun onPostalCodeChange(value: String) { _uiState.value = _uiState.value.copy(postalCode = value) }
    fun onPrefectureChange(value: String) { _uiState.value = _uiState.value.copy(prefecture = value) }
    fun onCityChange(value: String) { _uiState.value = _uiState.value.copy(city = value) }
    fun onLine1Change(value: String) { _uiState.value = _uiState.value.copy(line1 = value) }
    fun onLine2Change(value: String) { _uiState.value = _uiState.value.copy(line2 = value) }
    fun onPhoneChange(value: String) { _uiState.value = _uiState.value.copy(phone = value) }
    fun onIsDefaultChange(value: Boolean) { _uiState.value = _uiState.value.copy(isDefault = value) }

    fun save() {
        val state = _uiState.value
        val requiredFilled = state.fullName.isNotBlank() && state.postalCode.isNotBlank() &&
            state.prefecture.isNotBlank() && state.city.isNotBlank() &&
            state.line1.isNotBlank() && state.phone.isNotBlank()
        if (!requiredFilled) return

        accountRepository.upsertAddress(
            Address(
                id = state.addressId ?: "",
                fullName = state.fullName,
                postalCode = state.postalCode,
                prefecture = state.prefecture,
                city = state.city,
                line1 = state.line1,
                line2 = state.line2,
                phone = state.phone,
                isDefault = state.isDefault,
            ),
        )
        _uiState.value = state.copy(saved = true)
    }
}
