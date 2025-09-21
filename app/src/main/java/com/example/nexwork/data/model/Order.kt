package com.example.nexwork.data.model

data class Order(
    val id: String,
    val serviceName: String,
    val clientName: String,
    val date: String,
    val time: String,
    val imageUrl: String
)