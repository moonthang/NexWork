package com.example.nexwork.ui.orders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Order
import com.example.nexwork.data.repository.OrderRepository

class OrderViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    private val _createOrderResult = MutableLiveData<Result<Unit>>()
    val createOrderResult: LiveData<Result<Unit>> = _createOrderResult

    fun createOrder(order: Order) {
        orderRepository.createOrder(order) { result ->
            _createOrderResult.postValue(result)
        }
    }
}