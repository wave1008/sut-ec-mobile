package com.sutec.mobile.feature.orders

import androidx.lifecycle.ViewModel
import com.sutec.mobile.data.model.Order
import com.sutec.mobile.data.repository.OrderRepository
import kotlinx.coroutines.flow.StateFlow

class OrdersViewModel(
    orderRepository: OrderRepository,
) : ViewModel() {

    val orders: StateFlow<List<Order>> = orderRepository.orders
}
