package com.example.nexwork.ui.categories

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.Category
import com.example.nexwork.data.repository.CategoriesRepository
import com.google.firebase.storage.FirebaseStorage

class CategoriesViewModel : ViewModel() {

    private val repository = CategoriesRepository()
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories
    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory
    private val _operationStatus = MutableLiveData<Result<Unit>>()
    val operationStatus: LiveData<Result<Unit>> = _operationStatus
    private val _uploadImageUrl = MutableLiveData<Result<String>>()
    val uploadImageUrl: LiveData<Result<String>> = _uploadImageUrl

    fun createCategory(category: Category) {
        repository.createCategory(category) { result ->
            _operationStatus.postValue(result)
            if (result.isSuccess) loadCategories()
        }
    }

    fun updateCategory(category: Category) {
        repository.updateCategory(category) { result ->
            _operationStatus.postValue(result)
            if (result.isSuccess) loadCategories()
        }
    }

    fun deleteCategory(categoryId: String) {
        repository.deleteCategory(categoryId) { result ->
            _operationStatus.postValue(result)
            if (result.isSuccess) loadCategories()
        }
    }

    fun getCategoryById(categoryId: String) {
        repository.getCategoryById(categoryId) { result ->
            result.onSuccess { _selectedCategory.postValue(it) }
            result.onFailure { _selectedCategory.postValue(null) }
        }
    }

    fun loadCategories() {
        repository.getAllCategories { result ->
            result.onSuccess { _categories.postValue(it) }
            result.onFailure { _categories.postValue(emptyList()) }
        }
    }

    fun uploadCategoryImage(categoryId: String, imageUri: Uri) {
        // Identificar ubicación en Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
            .child("categories/$categoryId.jpg")

        // Subir la imagen
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // Obtener la URL de descarga
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
}