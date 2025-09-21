package com.example.nexwork.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    // Comunicacion del estado con la UI
    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun registerUser(
        firstName: String, lastName: String, email: String, birthDate: String,
        phone: String, password: String, userRole: String
    ) {
        _registrationState.value = RegistrationState.Loading

        repository.registerUser(email, password) { authResult ->
            authResult.onSuccess { firebaseUser ->
                val newUser = User(
                    userId = firebaseUser.uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    birthDate = birthDate,
                    phone = phone,
                    role = userRole
                )

                repository.saveUserData(newUser) { dbResult ->
                    dbResult.onSuccess {
                        _registrationState.value = RegistrationState.Success
                    }.onFailure { error ->
                        _registrationState.value = RegistrationState.Error(error.message ?: "Error al guardar datos")
                    }
                }
            }.onFailure { error ->
                _registrationState.value = RegistrationState.Error(error.message ?: "Error en el registro")
            }
        }
    }
}

// Clase para manejar los estados
sealed class RegistrationState {
    object Loading : RegistrationState()
    object Success : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}