package com.chat.data.repository

import com.chat.data.source.local.ChatLocalDataSource
import com.chat.data.source.remote.ChatRemoteDataSource
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import com.core.extension.emitFlow
import com.core.room.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


class ChatRepositoryImplTest {
    @Test
    fun `given connect() is called, when client connect successfully, then return ConnectionState_Connected`() =
        runTest {
            //arrange
            val expected = ConnectionStatus.Connected
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(remoteDataSource.connect()).thenReturn(emitFlow { ConnectionStatus.Connected })
            //act
            val result = repository.connect().singleOrNull()

            //assert
            assertEquals(expected, result)
        }

    @Test
    fun `given connect() is called, when client connect fail, then return ConnectionState_Failed`() =
        runTest {
            //arrange
            val ex = Exception()
            val expected = ConnectionStatus.Failed(ex)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(remoteDataSource.connect()).thenReturn(emitFlow { ConnectionStatus.Failed(ex) })
            //act
            val result = repository.connect().singleOrNull()

            //assert
            assertEquals(expected, result)
        }

    @Test
    fun `given loadHistory() is called, when there is cache, then return history message list`() =
        runTest {
            //arrange
            val msgEntity = MessageEntity(id = "123", text = "msg", dateTime = 123)
            val msg = Message(id = "123", text = "msg", dateTime = 123)
            val expected = listOf(msg)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(localDataSource.loadHistory()).thenReturn(emitFlow { listOf(msgEntity) })
            //act
            val result = repository.loadHistory().singleOrNull()

            //assert
            assertEquals(expected, result)
        }

    @Test
    fun `given loadHistory() is called, when there is no cache, then return empty message list`() =
        runTest {
            //arrange
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(localDataSource.loadHistory()).thenReturn(emitFlow { listOf() })
            //act
            val result = repository.loadHistory().single()

            //assert
            assert(result.isEmpty())
        }

    @Test(expected = Throwable::class)
    fun `given loadHistory() is called, when load history fail, then return error`() =
        runTest {
            //arrange
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(localDataSource.loadHistory()).thenThrow(Throwable())
            //act
            repository.loadHistory().single()
        }

    @Test
    fun `given sendMessage() is called, when message sent successfully, then return the sent message`() =
        runTest {
            //arrange
            val msg = "msg"
            val expected = Message(id = "123", text = msg, dateTime = 123)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(remoteDataSource.sendMessage(msg)).thenReturn(emitFlow { expected })

            //act
            val result = repository.sendMessage(msg).singleOrNull()

            //assert
            assertEquals(expected.text, result?.text)
        }

    @Test
    fun `given sendMessage() is called, when message sent successfully, then local_saveMsg should be called`() =
        runTest {
            //arrange
            val msg = "msg"
            val msgEntity = MessageEntity(id = "123", text = msg, dateTime = 123)
            val expected = Message(id = "123", text = msg, dateTime = 123)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            `when`(remoteDataSource.sendMessage(msg)).thenReturn(emitFlow { expected })

            //act
            repository.sendMessage(msg).singleOrNull()

            //assert
            verify(localDataSource, times(1)).saveMessage(msgEntity)
        }

    @Test
    fun `given receiveMessage() is called, when there is incoming message, then return the received message`() =
        runTest {
            //arrange
            val msg = "msg"
            val sharedFlow = MutableSharedFlow<Message>(replay = 1)
            val expected = Message(id = "123", text = msg, dateTime = 123)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            sharedFlow.tryEmit(expected)
            `when`(remoteDataSource.receiveMessage()).thenReturn(sharedFlow)

            //act
            val result = repository.receiveMessage().first()

            //assert
            assertEquals(expected.text, result.text)
        }

    @Test
    fun `given receiveMessage() is called, when there is incoming message, then local_saveMsg should be called`() =
        runTest {
            //arrange
            val msg = "msg"
            val msgEntity = MessageEntity(id = "123", text = msg, dateTime = 123)
            val expected = Message(id = "123", text = msg, dateTime = 123)
            val sharedFlow = MutableSharedFlow<Message>(replay = 1)
            val localDataSource = mock(ChatLocalDataSource::class.java)
            val remoteDataSource = mock(ChatRemoteDataSource::class.java)
            val dispatcher = Dispatchers.Unconfined
            val repository = ChatRepositoryImpl(remoteDataSource, localDataSource, dispatcher)

            sharedFlow.tryEmit(expected)
            `when`(remoteDataSource.receiveMessage()).thenReturn(sharedFlow)

            //act
            repository.receiveMessage().first()

            //assert
            verify(localDataSource, times(1)).saveMessage(msgEntity)
        }
}