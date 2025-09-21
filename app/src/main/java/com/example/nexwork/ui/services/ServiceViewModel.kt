package com.example.nexwork.ui.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.ServiceRepository

class ServiceViewModel : ViewModel() {

    private val repository = ServiceRepository()

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    fun fetchServices() {
        repository.getAllServices { result ->
            result.onSuccess { serviceList ->
                _services.value = serviceList
            }
        }
    }
}