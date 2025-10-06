package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CategoriesRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val categoriesCollection = db.collection("categories")


    // Crear
    fun createCategory(category: Category, onComplete: (Result<Unit>) -> Unit) {
        val categoryData = mapOf(
            "categoryId" to category.categoryId,
            "name" to category.name,
            "imageUrl" to category.imageUrl,
            "createdAt" to Calendar.getInstance().time
        )

        categoriesCollection.document(category.categoryId).set(categoryData)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Actualizar
    fun updateCategory(category: Category, onComplete: (Result<Unit>) -> Unit) {
        categoriesCollection.document(category.categoryId).set(category)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener categoría por su ID
    fun getCategoryById(categoryId: String, onComplete: (Result<Category>) -> Unit) {
        categoriesCollection.document(categoryId).get()
            .addOnSuccessListener { document ->
                val category = document.toObject(Category::class.java)
                if (category != null) {
                    onComplete(Result.success(category))
                } else {
                    onComplete(Result.failure(Exception("Categoría con ID $categoryId no encontrada")))
                }
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener todas las categorías
    fun getAllCategories(onComplete: (Result<List<Category>>) -> Unit) {
        categoriesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.toObjects(Category::class.java)
                onComplete(Result.success(categories))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Eliminar
    fun deleteCategory(categoryId: String, onComplete: (Result<Unit>) -> Unit) {
        categoriesCollection.document(categoryId).delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener el ID y el Nombre de todas las categorías
    fun getCategoryIdsAndNames(onComplete: (Result<List<Pair<String, String>>>) -> Unit) {
        categoriesCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val idAndNames = querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    Pair(id, name)
                }
                onComplete(Result.success(idAndNames))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }
}