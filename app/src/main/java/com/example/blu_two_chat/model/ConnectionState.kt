package com.example.blu_two_chat.model

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting   : ConnectionState()
    data object Connected    : ConnectionState()
    data class Failed(val message: String) : ConnectionState()
}