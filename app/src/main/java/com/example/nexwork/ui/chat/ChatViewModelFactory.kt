package com.example.nexwork.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.ChatRepository

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Identifica los repositorios necesarios para ChatViewModel
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatRepository, authRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}