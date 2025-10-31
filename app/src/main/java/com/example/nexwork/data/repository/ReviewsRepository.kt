package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Reviews
import com.google.firebase.firestore.FirebaseFirestore

class ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("reviews")

    // Crear una review
    fun createReview (review: Reviews, onComplete: (Result<Unit>) -> Unit){
        reviewsCollection.document(review.reviewId).set(review)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener review por id de servicio
    fun getReviewById(reviewId: String, onComplete: (Result<Reviews>) -> Unit) {
        reviewsCollection.document(reviewId).get()
            .addOnSuccessListener { document ->
                val review = document.toObject(Reviews::class.java)
                if (review != null) {
                    onComplete(Result.success(review))
                } else {
                    onComplete(Result.failure(Exception("Review no encontrada")))
                }
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener todas las reviews de un servicio
    fun getReviewsByServiceId(serviceId: String, onComplete: (Result<List<Reviews>>) -> Unit) {
        reviewsCollection
            .whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val reviewsList = querySnapshot.documents.mapNotNull { it.toObject(Reviews::class.java) }
                onComplete(Result.success(reviewsList))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    // Actualizar una review
    fun updateReview(review: Reviews, onComplete: (Result<Unit>) -> Unit) {
        reviewsCollection.document(review.reviewId).set(review)
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    // Eliminar una review
    fun deleteReview(reviewId: String, onComplete: (Result<Unit>) -> Unit) {
        reviewsCollection.document(reviewId).delete()
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }
}