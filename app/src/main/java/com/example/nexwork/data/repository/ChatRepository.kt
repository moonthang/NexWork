package com.example.nexwork.data.repository

import com.example.nexwork.data.model.Chat
import com.example.nexwork.data.model.Message
import com.example.nexwork.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Obtener todos los chats del usuario actual
    suspend fun getChats(): List<Chat> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("chats")
            .whereArrayContains("participantsIds", userId)
            .get()
            .await()
        return snapshot.toObjects(Chat::class.java)
    }

    // Obtener mensajes de un chat
    suspend fun getMessages(chatId: String): List<Message> {
        val snapshot = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()
        return snapshot.toObjects(Message::class.java)
    }

    // Enviar un mensaje
    suspend fun sendMessage(chatId: String, message: Message) {
        db.collection("chats").document(chatId)
            .collection("messages").add(message).await()

        val chatRef = db.collection("chats").document(chatId)
        db.runBatch { batch ->
            batch.update(chatRef, "lastMessage", message.text)
            batch.update(chatRef, "lastMessageTimestamp", message.timestamp)
        }.await()
    }

    // Obtener todos los usuarios excepto el actual
    suspend fun getAllUsersForSearch(): List<User> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = db.collection("users").get().await()
        return snapshot.toObjects(User::class.java).filter { it.userId != currentUserId }
    }

    // Crea el chat si no existe
    suspend fun createChatIfNotExists(chatId: String, participantIds: List<String>) {
        val chatRef = db.collection("chats").document(chatId)
        val chatData = mapOf("participantsIds" to participantIds)
        chatRef.set(chatData, SetOptions.merge()).await()
    }
}