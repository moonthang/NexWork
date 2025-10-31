package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Favorites
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val favoritesCollection = db.collection("favorites")

    // Agregar un favorito
    fun addFavorite(favorite: Favorites, onComplete: (Result<Unit>) -> Unit) {
        favoritesCollection.document(favorite.favoriteId).set(favorite)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener todos los favoritos de un usuario
    fun getFavoritesByUserId(userId: String, onComplete: (Result<List<Favorites>>) -> Unit) {
        favoritesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val favoritesList = querySnapshot.documents.mapNotNull { it.toObject(Favorites::class.java) }
                onComplete(Result.success(favoritesList))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    // Eliminar un favorito
    fun deleteFavorite(favoriteId: String, onComplete: (Result<Unit>) -> Unit) {
        favoritesCollection.document(favoriteId).delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Verificar si un servicio ya está en favoritos
    fun isFavorite(userId: String, serviceId: String, onComplete: (Result<Favorites?>) -> Unit) {
        favoritesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("serviceId", serviceId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val favorite = querySnapshot.documents.firstOrNull()?.toObject(Favorites::class.java)
                onComplete(Result.success(favorite))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }
}