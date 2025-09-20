package com.example.nexwork.ui.orders

data class Order(
    val id: String,
    val serviceName: String,
    val clientName: String,
    val date: String,
    val time: String,
    val imageUrl: String
)