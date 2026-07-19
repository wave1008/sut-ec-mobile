package com.sutec.mobile.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Address
import com.sutec.mobile.data.model.CartItem
import com.sutec.mobile.data.model.OrderTotals
import com.sutec.mobile.data.model.PaymentMethod
import com.sutec.mobile.data.model.PaymentType
import com.sutec.mobile.data.repository.AccountRepository
import com.sutec.mobile.data.repository.CartRepository
import com.sutec.mobile.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val totals: OrderTotals = OrderTotals(0, 0, 0),
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val payments: List<PaymentMethod> = emptyList(),
    val selectedPaymentId: String? = null,
    val placing: Boolean = false,
    val placedOrderId: String? = null,
)

// 支払い方法の表示ラベル。CASH_ON_DELIVERY は tr() を使わず言語非依存の固定表記にする(注文データの一部として保存されるため)。
fun paymentDisplayLabel(payment: PaymentMethod): String = when (payment.type) {
    PaymentType.CARD -> "${payment.brand} •••• ${payment.last4}"
    PaymentType.CASH_ON_DELIVERY -> "代金引換(COD)"
}

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val accountRepository: AccountRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                cartRepository.items,
                cartRepository.totals,
                accountRepository.addresses,
                accountRepository.paymentMethods,
            ) { items, totals, addresses, payments ->
                CartAccountSnapshot(items, totals, addresses, payments)
            }.collect { snapshot ->
                // 選択済みなら維持(住所/支払い一覧の再取得で初期選択が上書きされないように)。
                _uiState.update { state ->
                    state.copy(
                        items = snapshot.items,
                        totals = snapshot.totals,
                        addresses = snapshot.addresses,
                        selectedAddressId = state.selectedAddressId
                            ?: snapshot.addresses.firstOrNull { it.isDefault }?.id
                            ?: snapshot.addresses.firstOrNull()?.id,
                        payments = snapshot.payments,
                        selectedPaymentId = state.selectedPaymentId
                            ?: snapshot.payments.firstOrNull { it.isDefault }?.id
                            ?: snapshot.payments.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun selectAddress(id: String) {
        _uiState.update { it.copy(selectedAddressId = id) }
    }

    fun selectPayment(id: String) {
        _uiState.update { it.copy(selectedPaymentId = id) }
    }

    fun placeOrder() {
        val state = _uiState.value
        if (state.placing || state.items.isEmpty()) return
        val addressId = state.selectedAddressId ?: return
        val paymentId = state.selectedPaymentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(placing = true) }
            // 金額・住所ラベルはサーバーが権威決定。client は選択IDのみ渡す。
            runCatching { orderRepository.placeOrder(addressId, paymentId) }
                .onSuccess { order ->
                    cartRepository.clear()
                    _uiState.update { it.copy(placing = false, placedOrderId = order.id) }
                }
                .onFailure { _uiState.update { it.copy(placing = false) } }
        }
    }
}

private data class CartAccountSnapshot(
    val items: List<CartItem>,
    val totals: OrderTotals,
    val addresses: List<Address>,
    val payments: List<PaymentMethod>,
)
