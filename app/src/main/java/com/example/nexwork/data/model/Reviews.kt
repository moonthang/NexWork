package com.example.nexwork.data.model

data class Reviews(
    var reviewId: String = "",
    val serviceId: String = "",
    val userId: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)