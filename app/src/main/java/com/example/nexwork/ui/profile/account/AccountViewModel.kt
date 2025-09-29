package com.example.nexwork.ui.profile.account

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.storage.FirebaseStorage

class AccountViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData
    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading
    private val _passwordChangeResult = MutableLiveData<Result<String>>()
    val passwordChangeResult: LiveData<Result<String>> = _passwordChangeResult
    private val _deleteAccountError = MutableLiveData<Exception>()
    val deleteAccountError: LiveData<Exception> = _deleteAccountError

    fun loadUserData() {
        val uid = repository.getCurrentUserId() ?: return
        _loading.postValue(true)
        repository.getUserById(uid) { result ->
            _loading.postValue(false)
            result.onSuccess { user ->
                _userData.postValue(user)
                _profileImageUrl.postValue(user.profileImageUrl)
            }
        }
    }

    fun updateUser(updated: User) {
        _loading.postValue(true)
        repository.updateUser(updated) { result ->
            _loading.postValue(false)
            result.onSuccess {
                _userData.postValue(updated)
                _profileImageUrl.postValue(updated.profileImageUrl)
            }
        }
    }

    fun updateUserWithEmailCheck(updated: User) {
        val currentUser = auth.currentUser ?: return
        val oldEmail = _userData.value?.email

        if (oldEmail == updated.email) {
            updateUser(updated)
            return
        }

        _loading.postValue(true)

        currentUser.updateEmail(updated.email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    repository.updateUser(updated) { result ->
                        _loading.postValue(false)
                        result.onSuccess {
                            _userData.postValue(updated)
                            _profileImageUrl.postValue(updated.profileImageUrl)
                        }
                        result.onFailure {
                            currentUser.updateEmail(oldEmail!!)
                            _loading.postValue(false)
                        }
                    }
                } else {
                    _loading.postValue(false)
                }
            }
    }


    fun updateProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val role = userData.value?.role ?: "client"

        val storageRef = FirebaseStorage.getInstance().reference
            .child("$role/$userId/profile/profile.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val current = userData.value ?: return@addOnSuccessListener
                    val updated = current.copy(profileImageUrl = uri.toString())
                    updateUser(updated)
                }
            }
    }


    fun toggleUserRole() {
        val current = _userData.value ?: return
        val newRole = if (current.role == "provider") "client" else "provider"
        val updated = current.copy(role = newRole)
        updateUser(updated)
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmNewPassword.isBlank()) {
            _passwordChangeResult.postValue(Result.failure(Exception("Todos los campos son obligatorios")))
            return
        }

        if (newPassword != confirmNewPassword) {
            _passwordChangeResult.postValue(Result.failure(Exception("Las nuevas contraseñas no coinciden")))
            return
        }

        if (newPassword.length < 6) {
            _passwordChangeResult.postValue(Result.failure(Exception("La nueva contraseña debe tener al menos 6 caracteres")))
            return
        }

        val user = auth.currentUser
        if (user == null || user.email == null) {
            _passwordChangeResult.postValue(Result.failure(Exception("Usuario no autenticado o email no disponible")))
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateFirebasePassword(newPassword)
                } else {
                    _passwordChangeResult.postValue(Result.failure(Exception("Error de re-autenticación: Contraseña incorrecta.")))
                }
            }
    }

    private fun updateFirebasePassword(newPassword: String) {
        val user = auth.currentUser ?: return

        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordChangeResult.postValue(Result.success("Contraseña actualizada con éxito"))
                } else {
                    val exception = task.exception
                    val message = when (exception) {
                        is FirebaseAuthRecentLoginRequiredException -> "Error: Sesión expirada, intente de nuevo."
                        else -> "Error al cambiar la contraseña: ${exception?.message}"
                    }
                    _passwordChangeResult.postValue(Result.failure(Exception(message)))
                }
            }
    }

    private fun deleteUserStorage(uid: String, role: String?, onComplete: () -> Unit) {
        val path = if (role == "provider") "provider/$uid" else "client/$uid"
        val storageRef = storage.reference.child(path)

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val deleteTasks = listResult.items.map { it.delete() }
                val folderDeleteTask = storageRef.delete()

                deleteTasks.plus(folderDeleteTask).forEach { task ->
                    task.addOnFailureListener {
                    }
                }
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    fun deleteUserWithReauthentication(currentPassword: String) {
        val firebaseUser = auth.currentUser
        val email = firebaseUser?.email
        val uid = firebaseUser?.uid
        val role = _userData.value?.role

        if (email == null || uid == null || role == null) {
            _deleteAccountError.postValue(Exception("No se pudo obtener la sesión activa o el rol del usuario."))
            return
        }

        _loading.postValue(true)

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        firebaseUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {

                    repository.deleteUser(uid) { firestoreResult ->
                        if (firestoreResult.isSuccess) {

                            deleteUserStorage(uid, role) {
                                firebaseUser.delete()
                                    .addOnCompleteListener { authTask ->
                                        _loading.postValue(false)
                                        if (authTask.isSuccessful) {
                                            auth.signOut()
                                            _userData.postValue(null)
                                            _profileImageUrl.postValue(null)
                                        } else {
                                            auth.signOut()
                                            _userData.postValue(null)
                                            _deleteAccountError.postValue(Exception("Error crítico al eliminar la cuenta de autenticación."))
                                        }
                                    }
                            }
                        } else {
                            _loading.postValue(false)
                            firestoreResult.onFailure { exception ->
                                _deleteAccountError.postValue(
                                    Exception("Error al eliminar datos de Firestore: ${exception.message}. Verifique sus reglas.")
                                )
                            }
                        }
                    }
                } else {
                    _loading.postValue(false)
                    _deleteAccountError.postValue(Exception("La contraseña ingresada es incorrecta. No se puede eliminar la cuenta."))
                }
            }
    }

    fun logout() {
        auth.signOut()
        _userData.postValue(null)
        _profileImageUrl.postValue(null)
    }
}