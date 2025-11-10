package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    fun createOrder(order: Order, onComplete: (Result<Unit>) -> Unit) {
        val newOrder = if (order.orderId.isEmpty()) {
            order.orderId = ordersCollection.document().id
            order
        } else {
            order
        }

        ordersCollection.document(newOrder.orderId).set(newOrder)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }
}