package com.example.nexwork.data.repository

import com.example.nexwork.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Registro de usuario en Auth
    fun registerUser(
        email: String,
        password: String,
        onComplete: (Result<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.user != null) {
                    onComplete(Result.success(task.result.user!!))
                } else {
                    onComplete(Result.failure(task.exception ?: Exception("Error en el registro")))
                }
            }
    }
    
    // CREATE (Se reutiliza parte del registro, pero se podría crear uno nuevo si hiciera falta)
    fun createUser(user: User, onComplete: (Result<Unit>) -> Unit) {
        usersCollection.document(user.userId).set(user)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // READ
    fun getUserById(userId: String, onComplete: (Result<User>) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    onComplete(Result.success(user))
                } else {
                    onComplete(Result.failure(Exception("User not found")))
                }
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // Obtener todos los usuarios
    fun getAllUsers(onComplete: (Result<List<User>>) -> Unit) {
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.toObjects(User::class.java)
                onComplete(Result.success(users))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // UPDATE
    fun updateUser(user: User, onComplete: (Result<Unit>) -> Unit) {
        usersCollection.document(user.userId).set(user)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }

    // DELETE
    fun deleteUser(userId: String, onComplete: (Result<Unit>) -> Unit) {
        usersCollection.document(userId).delete()
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }


    // Registro de usuario en Firestore
    fun saveUserData(
        user: User,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val userProfile = mapOf(
            "userId" to user.userId,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "email" to user.email,
            "birthDate" to user.birthDate,
            "phone" to user.phone,
            "role" to user.role,
            "createdAt" to Calendar.getInstance().time
        )

        usersCollection.document(user.userId).set(userProfile)
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                auth.currentUser?.delete()
                onComplete(Result.failure(e))
            }
    }

    // Obtener el ID del usuario logeado
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Obtener el ID y el Nombre de los usuarios
    fun getUsersIdsAndNames(onComplete: (Result<List<Pair<String, String>>>) -> Unit) {
        usersCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val idAndNames = querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("firstName") ?: ""
                    Pair(id, name)
                }
                onComplete(Result.success(idAndNames))
            }
            .addOnFailureListener { e -> onComplete(Result.failure(e)) }
    }
}