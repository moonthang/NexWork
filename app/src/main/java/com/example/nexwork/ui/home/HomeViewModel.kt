package com.example.nexwork.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.ServiceRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val authRepository = AuthRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    fun fetchLastThreeServices() {
        viewModelScope.launch {
            val providerId = authRepository.getCurrentUserId()
            if (providerId != null) {
                val result = serviceRepository.getServicesForProvider(providerId, 3)
                _services.postValue(result)
            }
        }
    }
}