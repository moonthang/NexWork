package com.example.nexwork.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.ServiceHistory
import com.example.nexwork.data.model.ServiceStatus
import java.util.Date

class ServiceHistoryViewModel : ViewModel() {

    private val _serviceHistory = MutableLiveData<List<ServiceHistory>>()
    val serviceHistory: LiveData<List<ServiceHistory>> = _serviceHistory

    fun fetchServiceHistory() {
        // Datos de prueba
        val dummyList = listOf(
            ServiceHistory("1", "Limpieza de GYM", "Sofía Ramirez", "Clean & Refresh", Date(), ServiceStatus.PENDIENTE, ""),
            ServiceHistory("2", "Jardinería Residencial", "Carlos Vera", "Green Thumb", Date(), ServiceStatus.COMPLETADO, ""),
            ServiceHistory("3", "Mantenimiento de Piscina", "Ana Torres", "AquaClear", Date(), ServiceStatus.CANCELADO, "")
        )
        _serviceHistory.value = dummyList
    }
}