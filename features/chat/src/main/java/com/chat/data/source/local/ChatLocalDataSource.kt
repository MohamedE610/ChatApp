package com.chat.data.source.local

import com.core.extension.emitFlow
import com.core.room.dao.MessagesDao
import com.core.room.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(
    private val messageDao: MessagesDao
) {
    suspend fun loadHistory(): Flow<List<MessageEntity>> {
        return emitFlow { messageDao.getMessages() }
    }

    suspend fun saveMessage(msg: MessageEntity) {
        messageDao.saveMessage(msg)
    }

    suspend fun invalidateCache() {
        messageDao.invalidateCache()
    }
}