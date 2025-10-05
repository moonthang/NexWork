package com.example.nexwork.data.model

import java.util.Date

enum class ServiceStatus {
    PENDIENTE,
    COMPLETADO,
    CANCELADO
}

data class ServiceHistory(
    val id: String,
    val serviceName: String,
    val clientName: String,
    val providerName: String,
    val date: Date,
    val status: ServiceStatus,
    val imageUrl: String
)