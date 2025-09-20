package com.example.nexwork.core.models

data class Order(
    val id: String,
    val serviceName: String,
    val clientName: String,
    val date: String,
    val time: String,
    val imageUrl: String
)