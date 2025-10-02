package com.example.nexwork.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Category

class CategoryViewModel : ViewModel() {


    private val _categories = MutableLiveData<List<Category>>()


    val categories: LiveData<List<Category>> = _categories

    fun fetchCategories() {
        // Implementacion de logica con firebase HERE => TODO
        val dummyCategories = listOf(
            Category("1", "Hogar", "Servicios de limpieza y mantenimiento para tu casa.", ""),
            Category("2", "Oficina", "Soluciones de limpieza para espacios de trabajo.", ""),
            Category("3", "Automóvil", "Cuidado y limpieza detallada para tu vehículo.", ""),
            Category("4", "Jardinería", "Mantenimiento de áreas verdes y jardines.", "")
        )
        _categories.value = dummyCategories
    }
}