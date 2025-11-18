package com.example.nexwork.ui.profile.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.FavoritesRepository
import com.example.nexwork.data.repository.ServiceRepository
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private val favoritesRepository = FavoritesRepository()
    private val serviceRepository = ServiceRepository()
    private val authRepository = AuthRepository()
    private val _favoriteServices = MutableLiveData<List<Service>>()
    val favoriteServices: LiveData<List<Service>> get() = _favoriteServices
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchFavoriteServices() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            favoritesRepository.getFavoritesByUserId(userId) { result ->
                result.onSuccess { favorites ->
                    if (favorites.isEmpty()) {
                        _favoriteServices.postValue(emptyList())
                        return@onSuccess
                    }
                    val serviceIds = favorites.map { it.serviceId }
                    serviceRepository.getServicesByIds(serviceIds) { serviceResult ->
                        serviceResult.onSuccess { services ->
                            _favoriteServices.postValue(services.map { it.copy(isFavorite = true) })
                        }.onFailure {
                            _error.postValue("Error al obtener los detalles de los servicios: ${it.message}")
                        }
                    }
                }.onFailure {
                    _error.value = "Error al obtener los favoritos: ${it.message}"
                }
            }
        }
    }

    fun removeFavorite(service: Service) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            favoritesRepository.isFavorite(userId, service.serviceId) { result ->
                result.onSuccess { favorite ->
                    if (favorite != null) {
                        favoritesRepository.deleteFavorite(favorite.favoriteId) { deleteResult ->
                            deleteResult.onSuccess {
                                val currentList = _favoriteServices.value?.toMutableList()
                                currentList?.removeAll { it.serviceId == service.serviceId }
                                _favoriteServices.postValue(currentList)
                            }.onFailure {
                                _error.postValue("Error al eliminar el favorito: ${it.message}")
                            }
                        }
                    }
                }.onFailure {
                    _error.postValue("Error al buscar el favorito a eliminar: ${it.message}")
                }
            }
        }
    }
}