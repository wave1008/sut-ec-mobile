package com.sutec.mobile.feature.orders

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderDetailViewModel(
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    fun load(orderId: String) {
        _order.value = orderRepository.getOrder(orderId)
    }
}
