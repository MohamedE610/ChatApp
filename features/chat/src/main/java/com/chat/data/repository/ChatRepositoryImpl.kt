package com.chat.data.repository

import android.util.Log
import com.chat.data.mapper.map
import com.chat.data.source.local.ChatLocalDataSource
import com.chat.data.source.remote.ChatRemoteDataSource
import com.chat.domain.repository.ChatRepository
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import com.core.constant.IO_DISPATCHER_KEY
import com.core.extension.emitFlow
import com.core.extension.flatMapFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatLocalDataSource,
    @Named(IO_DISPATCHER_KEY) private val coroutineDispatcher: CoroutineDispatcher
) : ChatRepository {
    override suspend fun connect(): Flow<ConnectionStatus> {
        return remoteDataSource.connect()
    }

    override suspend fun loadHistory(): Flow<List<Message>> {
        return localDataSource.loadHistory()
            .map { messages -> messages.map { it.map() } }
    }

    override suspend fun sendMessage(msg: String): Flow<Message> {
        return remoteDataSource.sendMessage(msg)
            .flatMapFlow { cacheMessage(it) }
    }

    override suspend fun receiveMessage(): Flow<Message> {
        return remoteDataSource.receiveMessage()
            .flatMapFlow { cacheMessage(it) }
            .flowOn(coroutineDispatcher)
    }

    private suspend fun cacheMessage(msg: Message): Flow<Message> {
        localDataSource.saveMessage(msg.map())
        Log.d("chat_teg", "msg saved -> `${msg.text}")
        return emitFlow { msg }
    }

    override suspend fun clearCache() {
        withContext(coroutineDispatcher) {
            localDataSource.clearCache()
        }
    }

    override suspend fun disconnect() {
        withContext(coroutineDispatcher) {
            remoteDataSource.disconnect()
        }
    }
}