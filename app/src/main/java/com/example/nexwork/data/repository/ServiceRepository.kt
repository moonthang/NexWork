package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore

class ServiceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val servicesCollection = db.collection("services")

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
        servicesCollection.document(serviceId).delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }
}