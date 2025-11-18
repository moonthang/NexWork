package com.example.nexwork.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Chat(
    @DocumentId
    val chatId: String = "",
    val participantsIds: List<String> = emptyList(),
    val lastMessage: String? = null,
    val lastMessageTimestamp: Date? = null
)