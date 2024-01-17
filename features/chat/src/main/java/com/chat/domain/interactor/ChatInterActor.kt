package com.chat.domain.interactor

import com.chat.domain.repository.ChatRepository
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatInterActor @Inject constructor(
    private val repository: ChatRepository
) {
    suspend fun connect(): Flow<ConnectionStatus> {
        return repository.connect()
    }

    suspend fun loadHistory(): Flow<List<Message>> {
        return repository.loadHistory()
    }

    suspend fun sendMessage(msg: String): Flow<Message> {
        return repository.sendMessage(msg)
    }

    suspend fun receiveMessage(): Flow<Message> {
        return repository.receiveMessage()
    }

    suspend fun clearCache() {
        repository.clearCache()
    }

    suspend fun disconnect() {
        repository.disconnect()
    }
}