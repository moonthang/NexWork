package com.example.nexwork.ui.services

import android.app.Application
import android.location.Location
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.nexwork.data.model.Service
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.ServiceRepository
import com.example.nexwork.data.repository.CategoriesRepository
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceRepository = ServiceRepository()
    private val categoriesRepository = CategoriesRepository()
    private val authRepository = AuthRepository()
    private val _service = MutableLiveData<Service?>()
    val service: LiveData<Service?> get() = _service
    private val _provider = MutableLiveData<User?>()
    val provider: LiveData<User?> get() = _provider
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
    private val _uploadImageUrl = MutableLiveData<Result<Pair<Int, String>>>()
    val uploadImageUrl: LiveData<Result<Pair<Int, String>>> = _uploadImageUrl
    private val _uploadImagesResult = MutableLiveData<Result<List<Pair<Int, String>>>>()
    val uploadImagesResult: LiveData<Result<List<Pair<Int, String>>>> = _uploadImagesResult
    private val auth = FirebaseAuth.getInstance()
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    private val _serviceDeleted = MutableLiveData<Boolean>(false)
    val serviceDeleted: LiveData<Boolean> get() = _serviceDeleted
    private val _locationMap = MutableLiveData<Map<String, Double>?>()
    val locationMap: LiveData<Map<String, Double>?> = _locationMap
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(getApplication())
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult

    // Estados de carga
    sealed class UpsertState {
        object Loading : UpsertState()
        data class Uploading(val progress: Int) : UpsertState()
        object Success : UpsertState()
        data class Error(val message: String?) : UpsertState()
    }

    private val _upsertState = MutableLiveData<UpsertState?>()
    val upsertState: LiveData<UpsertState?> = _upsertState

    data class ServiceUpsertInput(
        val serviceId: String,
        val providerId: String,
        val title: String,
        val categoryId: String,
        val description: String = "",
        val existingImageUrls: List<String?> = emptyList(),
        val newImages: List<Pair<Int, Uri>> = emptyList(),
        val deletedImageUrls: List<String> = emptyList(),
        val createdAt: Long = System.currentTimeMillis(),
        val plans: List<com.example.nexwork.data.model.ServicePlan> = emptyList(),
        val addons: List<com.example.nexwork.data.model.ServiceAddon> = emptyList(),
        val requiresVisit: Boolean = false,
        val ubication: Map<String, Double> = emptyMap()
    )

    // Obtener servicio por id
    fun loadServiceById(serviceId: String) {
        _loading.value = true
        serviceRepository.getServiceById(serviceId) { result ->
            _loading.value = false
            result.onSuccess { loadedService ->
                _service.value = loadedService
                loadedService?.let {
                    loadProvider(it.providerId)
                }
            }
            result.onFailure { e ->
                _error.value = "Error al cargar el servicio: ${e.message}"
                _service.value = null
            }
        }
    }

    // Obtener proveedor por id
    private fun loadProvider(providerId: String) {
        authRepository.getUserById(providerId) { result ->
            result.onSuccess { user ->
                _provider.value = user
            }
            result.onFailure {
                _error.value = "Error al cargar el proveedor: ${it.message}"
                _provider.value = null
            }
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

    // Eliminar servicio
    fun deleteService(id: String) {
        _loading.value = true
        serviceRepository.deleteService(id) { result ->
            _loading.value = false
            result.onSuccess {
                _service.value = null
                _serviceDeleted.value = true
            }
            result.onFailure { _error.value = it.message }
        }
    }

    // Manejar evento de eliminación de servicio
    fun onServiceDeletedHandled() {
        _serviceDeleted.value = false
    }

    // Obtener una ubicación actual, si hay permisos
    @SuppressWarnings("MissingPermission")
    fun getLatestLocationOnce(
        hasFinePermission: Boolean,
        hasCoarsePermission: Boolean,
        onResult: (Map<String, Double>?) -> Unit
    ) {
        if (!hasFinePermission && !hasCoarsePermission) {
            onResult(null)
            return
        }

        // Verificar permisos de nuevo por seguridad
        val context = getApplication<Application>()
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            onResult(null)
            return
        }

        try {
            // Obtener ubicación
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val map =
                        mapOf("latitude" to location.latitude, "longitude" to location.longitude)
                    _locationMap.postValue(map)
                    onResult(map)
                } else {
                    try {
                        val cts = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cts.token
                        )
                            .addOnSuccessListener { loc ->
                                if (loc != null) {
                                    val map = mapOf(
                                        "latitude" to loc.latitude,
                                        "longitude" to loc.longitude
                                    )
                                    _locationMap.postValue(map)
                                    onResult(map)
                                } else {
                                    onResult(null)
                                }
                            }
                            .addOnFailureListener {
                                onResult(null)
                            }
                    } catch (e: SecurityException) {
                        onResult(null)
                    } catch (e: Exception) {
                        onResult(null)
                    }
                }
            }.addOnFailureListener {
                onResult(null)
            }
        } catch (e: SecurityException) {
            onResult(null)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    // Actualizar servicio
    fun upsertService(input: ServiceUpsertInput) {
        _upsertState.postValue(UpsertState.Loading)

        val serviceId =
            if (input.serviceId.isNotEmpty()) input.serviceId else java.util.UUID.randomUUID()
                .toString()

        // Construir lista de URLs de imágenes
        fun buildFinalImageUrls(
            existing: List<String?>,
            uploaded: Map<Int, String>,
            deleted: List<String>
        ): List<String> {
            val imageMap = mutableMapOf<Int, String>()
            existing.forEachIndexed { idx, url ->
                if (!url.isNullOrEmpty()) imageMap[idx] = url
            }

            deleted.forEach { url ->
                val keysToRemove = imageMap.filterValues { it == url }.keys
                keysToRemove.forEach { imageMap.remove(it) }
            }

            uploaded.forEach { (idx, url) -> imageMap[idx] = url }

            return imageMap.toSortedMap().values.toList()
        }

        if (input.newImages.isNotEmpty()) {
            val total = input.newImages.size
            val results = mutableMapOf<Int, String>()
            var failed = false
            var completed = 0

            val userId = auth.currentUser?.uid
            if (userId == null) {
                _upsertState.postValue(UpsertState.Error("Usuario no autenticado"))
                return
            }
            val role = userData.value?.role ?: "client"

            fun saveFinalService(finalServiceId: String, finalImageUrls: List<String>) {
                val service = Service(
                    serviceId = finalServiceId,
                    providerId = input.providerId,
                    title = input.title,
                    categoryId = input.categoryId,
                    description = input.description,
                    imageUrl = finalImageUrls,
                    createdAt = input.createdAt,
                    plans = input.plans,
                    addons = input.addons,
                    requiresVisit = input.requiresVisit,
                    ubication = input.ubication
                )

                // Decide create vs update
                if (input.serviceId.isNotEmpty()) {
                    serviceRepository.updateService(service) { result ->
                        result.onSuccess {
                            _upsertState.postValue(UpsertState.Success)
                            _service.postValue(service)
                        }
                        result.onFailure {
                            _upsertState.postValue(UpsertState.Error(it.message))
                        }
                    }
                } else {
                    serviceRepository.createService(service) { result ->
                        result.onSuccess {
                            _upsertState.postValue(UpsertState.Success)
                            _service.postValue(service)
                        }
                        result.onFailure {
                            _upsertState.postValue(UpsertState.Error(it.message))
                        }
                    }
                }
            }

            fun proceedAfterUploads(uploadedMap: Map<Int, String>) {
                val finalImageUrls = buildFinalImageUrls(
                    input.existingImageUrls,
                    uploadedMap,
                    input.deletedImageUrls
                )

                if (input.deletedImageUrls.isNotEmpty()) {
                    serviceRepository.deleteServiceImages(input.deletedImageUrls) { _ ->
                        saveFinalService(serviceId, finalImageUrls)
                    }
                } else {
                    saveFinalService(serviceId, finalImageUrls)
                }
            }

            input.newImages.forEach { pair ->
                val index = pair.first
                val uri = pair.second
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("$role/$userId/services/$serviceId/image_$index.jpg")

                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            results[index] = downloadUri.toString()
                            completed++
                            val progress = (completed * 100) / total
                            _upsertState.postValue(UpsertState.Uploading(progress))
                            if (completed == total && !failed) {
                                proceedAfterUploads(results)
                            }
                        }.addOnFailureListener { e ->
                            if (!failed) {
                                failed = true
                                _upsertState.postValue(UpsertState.Error(e.message))
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        if (!failed) {
                            failed = true
                            _upsertState.postValue(UpsertState.Error(e.message))
                        }
                    }
            }
        } else {
            fun saveFinalService(finalServiceId: String, finalImageUrls: List<String>) {
                val service = Service(
                    serviceId = finalServiceId,
                    providerId = input.providerId,
                    title = input.title,
                    categoryId = input.categoryId,
                    description = input.description,
                    imageUrl = finalImageUrls,
                    createdAt = input.createdAt,
                    plans = input.plans,
                    addons = input.addons,
                    requiresVisit = input.requiresVisit,
                    ubication = input.ubication
                )

                if (input.serviceId.isNotEmpty()) {
                    serviceRepository.updateService(service) { result ->
                        result.onSuccess {
                            _upsertState.postValue(UpsertState.Success)
                            _service.postValue(service)
                        }
                        result.onFailure {
                            _upsertState.postValue(UpsertState.Error(it.message))
                        }
                    }
                } else {
                    serviceRepository.createService(service) { result ->
                        result.onSuccess {
                            _upsertState.postValue(UpsertState.Success)
                            _service.postValue(service)
                        }
                        result.onFailure {
                            _upsertState.postValue(UpsertState.Error(it.message))
                        }
                    }
                }
            }

            fun proceedAfterUploads(uploadedMap: Map<Int, String>) {
                val finalImageUrls = buildFinalImageUrls(
                    input.existingImageUrls,
                    uploadedMap,
                    input.deletedImageUrls
                )
                if (input.deletedImageUrls.isNotEmpty()) {
                    serviceRepository.deleteServiceImages(input.deletedImageUrls) { _ ->
                        saveFinalService(serviceId, finalImageUrls)
                    }
                } else {
                    saveFinalService(serviceId, finalImageUrls)
                }
            }

            proceedAfterUploads(emptyMap())
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

    // Obtener todas las usuarios (id + nombre)
    fun getUsers() {
        _loading.value = true
        authRepository.getUsersIdsAndNames { result ->
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