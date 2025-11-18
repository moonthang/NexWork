package com.example.nexwork.ui.categories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Category
import com.example.nexwork.data.model.Favorites
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.CategoriesRepository
import com.example.nexwork.data.repository.FavoritesRepository
import com.example.nexwork.data.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class CategoryViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val categoriesRepository = CategoriesRepository()
    private val favoritesRepository = FavoritesRepository()
    private val auth = FirebaseAuth.getInstance()
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category
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

    // Obtner categoria por id
    fun getCategoryById(categoryId: String) {
        categoriesRepository.getCategoryById(categoryId) { result ->
            result.onSuccess {
                _category.value = it
            }
            result.onFailure {
                _error.value = it.message
            }
        }
    }

    // Llamar servicios que comparten categoria
    fun getServicesByCategoryId(categoryId: String, serviceIdToExclude: String) {
        val userId = auth.currentUser?.uid
        serviceRepository.getServicesByCategoryId(categoryId) { result ->
            result.onSuccess { services ->
                val filteredServices = services.filter { it.serviceId != serviceIdToExclude }
                if (userId == null) {
                    _servicesByCategory.value = filteredServices
                } else {
                    favoritesRepository.getFavoritesByUserId(userId) { favoriteResult ->
                        favoriteResult.onSuccess { favorites ->
                            val favoriteServiceIds = favorites.map { it.serviceId }.toSet()
                            val servicesWithFavorites = filteredServices.map { service ->
                                service.copy(isFavorite = favoriteServiceIds.contains(service.serviceId))
                            }
                            _servicesByCategory.value = servicesWithFavorites
                        }
                        favoriteResult.onFailure {
                            _servicesByCategory.value = filteredServices // fallback
                        }
                    }
                }
            }
            result.onFailure { _error.value = it.message }
        }
    }

    // Agregar favorito
    fun toggleFavorite(service: Service) {
        val userId = auth.currentUser?.uid ?: return

        val currentList = _servicesByCategory.value ?: emptyList()
        val updatedList = currentList.map {
            if (it.serviceId == service.serviceId) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                it
            }
        }
        _servicesByCategory.value = updatedList

        if (!service.isFavorite) {
            val favorite = Favorites(
                favoriteId = UUID.randomUUID().toString(),
                userId = userId,
                serviceId = service.serviceId
            )
            favoritesRepository.addFavorite(favorite) { result ->
                result.onFailure {
                    Log.e("Favoritos", "Error al agregar a favotiros", it)
                }
            }
        } else {
            favoritesRepository.isFavorite(userId, service.serviceId) { result ->
                result.onSuccess { favorite ->
                    if (favorite != null) {
                        favoritesRepository.deleteFavorite(favorite.favoriteId) { deleteResult ->
                            deleteResult.onFailure {
                                Log.e("Favoritos", "Error al eliminar de favotiros", it)
                            }
                        }
                    }
                }
            }
        }
    }
}