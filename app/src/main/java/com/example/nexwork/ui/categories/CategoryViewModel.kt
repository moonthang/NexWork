package com.example.nexwork.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Category
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.CategoriesRepository
import com.example.nexwork.data.repository.ServiceRepository

class CategoryViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val categoriesRepository = CategoriesRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _servicesByCategory = MutableLiveData<List<Service>>()
    val servicesByCategory: LiveData<List<Service>> = _servicesByCategory

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    // Llamar categorias
    fun fetchCategories() {
        _loading.value = true
        categoriesRepository.getAllCategories { result ->
            _loading.value = false
            if (result.isSuccess) {
                _categories.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
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