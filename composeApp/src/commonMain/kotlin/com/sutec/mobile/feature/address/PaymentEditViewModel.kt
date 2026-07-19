package com.sutec.mobile.feature.address

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PaymentEditUiState(
    val paymentId: String? = null,
    val type: PaymentType = PaymentType.CARD,
    val brand: String = "",
    // カード番号全体を保持し、保存時に末尾4桁を PaymentMethod.last4 に切り出す。
    val cardNumber: String = "",
    val holderName: String = "",
    val expMonth: String = "",
    val expYear: String = "",
    val isDefault: Boolean = false,
    val saved: Boolean = false,
)

class PaymentEditViewModel(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentEditUiState())
    val uiState: StateFlow<PaymentEditUiState> = _uiState.asStateFlow()

    fun load(paymentId: String?) {
        val existing = paymentId?.let { accountRepository.getPayment(it) }
        _uiState.value = if (existing != null) {
            PaymentEditUiState(
                paymentId = existing.id,
                type = existing.type,
                brand = existing.brand,
                // 保存済みは last4 のみ保持しているため編集時は末尾4桁を初期表示(全桁は再入力扱い)。
                cardNumber = existing.last4,
                holderName = existing.holderName,
                expMonth = if (existing.expMonth > 0) existing.expMonth.toString() else "",
                expYear = if (existing.expYear > 0) existing.expYear.toString() else "",
                isDefault = existing.isDefault,
            )
        } else {
            PaymentEditUiState()
        }
    }

    fun onTypeChange(type: PaymentType) { _uiState.value = _uiState.value.copy(type = type) }
    fun onBrandChange(value: String) { _uiState.value = _uiState.value.copy(brand = value) }
    fun onCardNumberChange(value: String) { _uiState.value = _uiState.value.copy(cardNumber = value) }
    fun onHolderNameChange(value: String) { _uiState.value = _uiState.value.copy(holderName = value) }
    fun onExpMonthChange(value: String) { _uiState.value = _uiState.value.copy(expMonth = value) }
    fun onExpYearChange(value: String) { _uiState.value = _uiState.value.copy(expYear = value) }
    fun onIsDefaultChange(value: Boolean) { _uiState.value = _uiState.value.copy(isDefault = value) }

    fun save() {
        val state = _uiState.value
        if (state.type == PaymentType.CARD && (state.cardNumber.isBlank() || state.holderName.isBlank())) return

        accountRepository.upsertPayment(
            PaymentMethod(
                id = state.paymentId ?: "",
                type = state.type,
                brand = state.brand,
                last4 = state.cardNumber.takeLast(4),
                holderName = state.holderName,
                expMonth = state.expMonth.toIntOrNull() ?: 0,
                expYear = state.expYear.toIntOrNull() ?: 0,
                isDefault = state.isDefault,
            ),
        )
        _uiState.value = state.copy(saved = true)
    }
}
