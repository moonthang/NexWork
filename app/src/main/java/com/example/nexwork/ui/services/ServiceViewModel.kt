package com.example.nexwork.ui.services

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.ServiceRepository
import com.example.nexwork.data.repository.CategoriesRepository
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ServiceViewModel : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val categoriesRepository = CategoriesRepository()
    private val usersRepository = AuthRepository()
    private val _service = MutableLiveData<Service?>()
    val service: LiveData<Service?> get() = _service
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> get() = _services
    private val _categories = MutableLiveData<List<Pair<String, String>>>()
    val categories: LiveData<List<Pair<String, String>>> get() = _categories
    private val _users = MutableLiveData<List<String>>()
    val users: LiveData<List<String>> get() = _users
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _uploadImageUrl = MutableLiveData<Result<String>>()
    val uploadImageUrl: LiveData<Result<String>> = _uploadImageUrl
    private val auth = FirebaseAuth.getInstance()
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    // Crear servicio
    fun createService(service: Service) {
        _loading.value = true
        serviceRepository.createService(service) { result ->
            _loading.value = false
            result.onSuccess { _service.value = service }
            result.onFailure { _error.value = it.message }
        }
    }

    // Obtener servicio por id
    fun getServiceById(id: String) {
        _loading.value = true
        serviceRepository.getServiceById(id) { result ->
            _loading.value = false
            result.onSuccess { _service.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    // Obtener todos los servicios
    fun getAllServices() {
        _loading.value = true
        serviceRepository.getAllServices { result ->
            _loading.value = false
            result.onSuccess { _services.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    // Actualizar servicio
    fun updateService(service: Service) {
        _loading.value = true
        serviceRepository.updateService(service) { result ->
            _loading.value = false
            result.onSuccess { _service.value = service }
            result.onFailure { _error.value = it.message }
        }
    }

    // Eliminar servicio
    fun deleteService(id: String) {
        _loading.value = true
        serviceRepository.deleteService(id) { result ->
            _loading.value = false
            result.onSuccess { _service.value = null }
            result.onFailure { _error.value = it.message }
        }
    }

    // Subir imagen
    fun uploadServiceImage(serviceId: String, imageUri: Uri, index: Int) {
        val userId = auth.currentUser?.uid ?: return
        val role = userData.value?.role ?: "client"
        val storageRef = FirebaseStorage.getInstance().reference
            .child("$role/$userId/services/$serviceId/image_$index.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    _uploadImageUrl.postValue(Result.success(uri.toString()))
                }.addOnFailureListener { e ->
                    _uploadImageUrl.postValue(Result.failure(e))
                }
            }
            .addOnFailureListener { e ->
                _uploadImageUrl.postValue(Result.failure(e))
            }
    }

    // Obtener todas las categorías (id + nombre)
    fun getCategories() {
        _loading.value = true
        categoriesRepository.getCategoryIdsAndNames { result ->
            _loading.value = false
            result.onSuccess { _categories.value = it }
            result.onFailure { _error.value = it.message }
        }
    }

    // Obtener todas las categorías (id + nombre)
    fun getUsers() {
        _loading.value = true
        usersRepository.getUsersIdsAndNames { result ->
            _loading.value = false
            result.onSuccess { usersList ->
                val formattedUsers: List<String> = usersList.map { pair ->
                    val id = pair.first
                    val name = pair.second
                    "$name ($id)"
                }
                _users.value = formattedUsers
            }
            result.onFailure { _error.value = it.message }
        }
    }
}