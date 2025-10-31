package com.example.nexwork.data.model

data class User(
    var userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val password: String = "",
    val profession: String = "",
    val role: String = "client",
    val profileImageUrl: String = ""
)