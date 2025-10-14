package com.example.nexwork.ui.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository

class UserViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _operationStatus = MutableLiveData<Result<Unit>>()
    val operationStatus: LiveData<Result<Unit>> = _operationStatus
    
    fun getAllUsers() {
        repository.getAllUsers { result ->
            result.onSuccess { userList ->
                _users.value = userList
            }

            // Opcional: Manejar el caso de fallo, por ejemplo, con otro LiveData
        }
    }

    fun getUserById(userId: String) {
        repository.getUserById(userId) { result ->
            result.onSuccess { user ->
                _user.value = user
            }
        }
    }

    fun createUser(user: User) {
        repository.createUser(user) { result ->
            _operationStatus.value = result
        }
    }
    
    fun updateUser(user: User) {
        repository.updateUser(user) { result ->
            _operationStatus.value = result
        }
    }

    fun deleteUser(userId: String) {
        repository.deleteUser(userId) { result ->
            _operationStatus.value = result
        }
    }
}
