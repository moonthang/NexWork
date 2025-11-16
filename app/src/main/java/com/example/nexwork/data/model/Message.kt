package com.example.nexwork.data.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Message(
    @DocumentId
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date = Date()
)