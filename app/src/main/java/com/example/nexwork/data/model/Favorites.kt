package com.example.nexwork.data.model

data class Favorites(
    var favoriteId: String = "",
    val userId: String = "",
    val serviceId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)