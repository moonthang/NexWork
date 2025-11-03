package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Service
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ServiceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val servicesCollection = db.collection("services")
    private val storage = FirebaseStorage.getInstance()

    // Crear un servicio en Firestore
    fun createService(service: Service, onComplete: (Result<Unit>) -> Unit) {
        servicesCollection.document(service.serviceId).set(service)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener un servicio por ID
    fun getServiceById(serviceId: String, onComplete: (Result<Service>) -> Unit) {
        servicesCollection.document(serviceId).get()
            .addOnSuccessListener { document ->
                val service = document.toObject(Service::class.java)
                if (service != null) {
                    onComplete(Result.success(service))
                } else {
                    onComplete(Result.failure(Exception("Servicio no encontrado")))
                }
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener todos los servicios
    fun getAllServices(onComplete: (Result<List<Service>>) -> Unit) {
        servicesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.toObjects(Service::class.java)
                onComplete(Result.success(services))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener servicios por ID de categoría
    fun getServicesByCategoryId(categoryId: String, onComplete: (Result<List<Service>>) -> Unit) {
        servicesCollection.whereEqualTo("categoryId", categoryId).get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.toObjects(Service::class.java)
                onComplete(Result.success(services))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Actualizar un servicio
    fun updateService(service: Service, onComplete: (Result<Unit>) -> Unit) {
        servicesCollection.document(service.serviceId).set(service)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Eliminar un servicio
    fun deleteService(serviceId: String, onComplete: (Result<Unit>) -> Unit) {
        val docRef = servicesCollection.document(serviceId)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                onComplete(Result.success(Unit))
                return@addOnSuccessListener
            }

            val service = documentSnapshot.toObject(Service::class.java)
            val imageUrls = service?.imageUrl ?: emptyList()

            if (imageUrls.isEmpty()) {
                docRef.delete()
                    .addOnSuccessListener { onComplete(Result.success(Unit)) }
                    .addOnFailureListener { e -> onComplete(Result.failure(e)) }
            } else {
                val deleteTasks = imageUrls.mapNotNull { url ->
                    if (url.isNotEmpty()) {
                        storage.getReferenceFromUrl(url).delete()
                    } else {
                        null
                    }
                }

                Tasks.whenAll(deleteTasks)
                    .addOnSuccessListener {
                        docRef.delete()
                            .addOnSuccessListener { onComplete(Result.success(Unit)) }
                            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
                    }
                    .addOnFailureListener { e ->
                        onComplete(Result.failure(e))
                    }
            }
        }.addOnFailureListener { e ->
            onComplete(Result.failure(e))
        }
    }

    // Borrar varias imágenes en Storage por sus URLs
    fun deleteServiceImages(urls: List<String>, onComplete: (Result<Unit>) -> Unit) {
        if (urls.isEmpty()) {
            onComplete(Result.success(Unit))
            return
        }

        val deleteTasks = urls.mapNotNull { url ->
            if (url.isNotEmpty()) {
                try {
                    storage.getReferenceFromUrl(url).delete()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

        if (deleteTasks.isEmpty()) {
            onComplete(Result.success(Unit))
            return
        }

        Tasks.whenAll(deleteTasks)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }
}