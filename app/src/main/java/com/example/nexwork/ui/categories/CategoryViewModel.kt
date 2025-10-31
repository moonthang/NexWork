package com.example.nexwork.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Category
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.ServiceRepository

class CategoryViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private val _servicesByCategory = MutableLiveData<List<Service>>()
    val servicesByCategory: LiveData<List<Service>> = _servicesByCategory
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchCategories() {
        // Implementacion de logica con firebase HERE => TODO
        val dummyCategories = listOf(
            Category("1", "Hogar", ""),
            Category("2", "Oficina", ""),
            Category("3", "Automóvil", ""),
            Category("4", "Jardinería", "")
        )
        _categories.value = dummyCategories
    }

    // Llamar servicios que comparten categoria
    fun getServicesByCategoryId(categoryId: String, serviceIdToExclude: String) {
        serviceRepository.getServicesByCategoryId(categoryId) { result ->
            result.onSuccess { services ->
                _servicesByCategory.value = services.filter { it.serviceId != serviceIdToExclude }
            }
            result.onFailure { _error.value = it.message }
        }
    }
}