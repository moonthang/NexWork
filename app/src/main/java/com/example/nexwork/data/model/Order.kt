package com.example.nexwork.data.model

import kotlin.Long

data class Order(
    var orderId: String = "",
    val serviceId: String = "",
    val titleService: String = "",
    val categoryId: String = "",
    val titleCategory: String = "",
    val providerId: String = "",
    val clientId: String = "",
    val date: Long = System.currentTimeMillis(),
    val time: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val ubicationClient: Map<String, Double> = emptyMap()
    val providerName: String,
    val clientName: String,
    val status: String,
)