package com.chat.domain.repository

import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun connect(): Flow<ConnectionStatus>
    suspend fun loadHistory(): Flow<List<Message>>
    suspend fun sendMessage(msg: String): Flow<Message>
    suspend fun receiveMessage(): Flow<Message>
    suspend fun invalidateCache()
    suspend fun disconnect()
}