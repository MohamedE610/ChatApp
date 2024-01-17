package com.chat.entity

sealed class ConnectionStatus {
    data object Connected : ConnectionStatus()
    data object Disconnected : ConnectionStatus()
    data class Failed(val e: Exception) : ConnectionStatus()
}

data class Message(
    val id: String,
    val text: String,
    val dateTime: Long
)