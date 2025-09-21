package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Service
import com.google.firebase.firestore.FirebaseFirestore

class ServiceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val servicesCollection = db.collection("services")

    fun getAllServices(onComplete: (Result<List<Service>>) -> Unit) {
        servicesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val services = querySnapshot.toObjects(Service::class.java)
                onComplete(Result.success(services))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }
    // Aquí irían las otras funciones del CRUD para servicios (create, update, delete)
}