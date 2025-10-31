package com.example.nexwork.data.model

data class Service(
    var serviceId: String = "",
    val providerId: String = "",
    val title: String = "",
    val categoryId: String = "",
    val description: String = "",
    val imageUrl: List<String> = emptyList(),
    val ReviewCount: Int = 0,
    val ReviewValue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val ubication: Map<String, Double> = emptyMap(),
    val plans: List<ServicePlan> = emptyList(),
    val addons: List<ServiceAddon> = emptyList()
)

data class ServicePlan(
    val planName: String = "",
    val planDescription: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "",
    val features: List<String> = emptyList()
)

data class ServiceAddon(
    val addonTitle: String = "",
    val addonDescription: String = ""
)