package com.example.nexwork.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun saveUserData(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        birthDate: String,
        phone: String,
        userRole: String,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val userProfile = hashMapOf(
            "userId" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "birthDate" to birthDate,
            "phone" to phone,
            "role" to userRole,
            "createdAt" to Calendar.getInstance().time
        )

        db.collection("users").document(userId).set(userProfile)
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                auth.currentUser?.delete()
                onComplete(Result.failure(e))
            }
    }
}