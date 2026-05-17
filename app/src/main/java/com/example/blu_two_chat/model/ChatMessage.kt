package com.example.blu_two_chat.model

data class ChatMessage (
    val text: String,
    val isMine: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)