package com.example.nexwork.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexwork.data.model.Chat
import com.example.nexwork.data.model.Message
import com.example.nexwork.data.model.User
import com.example.nexwork.data.repository.AuthRepository
import com.example.nexwork.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chatsWithUsers = MutableLiveData<List<ChatWithUser>>()
    val chatsWithUsers: LiveData<List<ChatWithUser>> = _chatsWithUsers
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> = _allUsers
    private val currentUserId: String?
        get() = authRepository.getCurrentUserId()

    // Obtener chats con usuarios
    fun getChats() {
        viewModelScope.launch {
            val chats = chatRepository.getChats()
            val chatsWithUserInfo = mutableListOf<ChatWithUser>()

            for (chat in chats) {
                val otherUserId = chat.participantsIds.find { it != currentUserId }
                if (otherUserId != null) {
                    authRepository.getUserById(otherUserId) { result ->
                        if (result.isSuccess) {
                            val user = result.getOrNull()
                            if (user != null) {
                                chatsWithUserInfo.add(ChatWithUser(chat, user))
                                _chatsWithUsers.value = chatsWithUserInfo
                            }
                        }
                    }
                }
            }
        }
    }

    // Obtener todos los usuarios
    fun fetchAllUsers() {
        viewModelScope.launch {
            _allUsers.value = chatRepository.getAllUsersForSearch()
        }
    }

    // Obtner mensajes
    fun getMessages(chatId: String) {
        viewModelScope.launch {
            _messages.value = chatRepository.getMessages(chatId)
        }
    }

    // Enviar mensaje
    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            val userId = currentUserId ?: return@launch
            val message = Message(senderId = userId, text = text)
            chatRepository.sendMessage(chatId, message)
            getMessages(chatId)
        }
    }

    // Crear chat si no existe
    suspend fun createChatIfNotExists(chatId: String, participantIds: List<String>) {
        chatRepository.createChatIfNotExists(chatId, participantIds)
    }
}