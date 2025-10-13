package com.example.nexwork.data.model

data class User(
    var userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val role: String = "user",
    val profileImageUrl: String = ""
)