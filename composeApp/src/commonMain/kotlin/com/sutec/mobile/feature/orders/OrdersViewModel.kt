package com.sutec.mobile.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val orders: List<Order> = emptyList(),
)

class OrdersViewModel(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        // 一覧は StateFlow を購読して反映。ロード成否は refresh() の結果で別管理。
        viewModelScope.launch {
            orderRepository.orders.collect { list -> _uiState.update { it.copy(orders = list) } }
        }
        load()
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            try {
                orderRepository.refresh()
                _uiState.update { it.copy(loading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun retry() = load()
}
