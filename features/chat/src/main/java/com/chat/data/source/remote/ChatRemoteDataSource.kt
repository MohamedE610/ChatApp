package com.chat.data.source.remote

import android.util.Log
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import com.core.constant.IO_DISPATCHER_KEY
import com.core.constant.WEB_SOCKET_URL_KEY
import com.core.extension.emitFlow
import dev.gustavoavila.websocketclient.WebSocketClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.net.URI
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ChatRemoteDataSource @Inject constructor(
    @Named(WEB_SOCKET_URL_KEY) private val url: String,
    @Named(IO_DISPATCHER_KEY) private val coroutineDispatcher: CoroutineDispatcher
) {

    private var webSocketClient: WebSocketClient? = null
    private val messageReceivedFlow = MutableSharedFlow<Message>(replay = 1)

    fun connect(): Flow<ConnectionStatus> {
        return callbackFlow {
            try {
                if (webSocketClient == null)
                    webSocketClient = object : WebSocketClient(URI(url)) {
                        override fun onOpen() {
                            Log.d("chat_tag", "connected")
                            trySend(ConnectionStatus.Connected)
                        }

                        override fun onTextReceived(message: String?) {
                            Log.d("chat_tag", "message received -> $message")
                            val msg = Message(
                                id = generateRandomId(),
                                text = message ?: "",
                                dateTime = Date().time
                            )
                            messageReceivedFlow.tryEmit(msg)
                        }

                        override fun onBinaryReceived(data: ByteArray?) {
                            Log.d("chat_tag", "onBinaryReceived")
                        }

                        override fun onPingReceived(data: ByteArray?) {
                            Log.d("chat_tag", "onPingReceived")
                        }

                        override fun onPongReceived(data: ByteArray?) {
                            Log.d("chat_tag", "onPongReceived")
                        }

                        override fun onException(ex: Exception?) {
                            Log.d("chat_tag", "failed, ex-> ${ex?.message}")
                            trySend(ConnectionStatus.Failed(ex ?: return))
                        }

                        override fun onCloseReceived(reason: Int, description: String?) {
                            Log.d("chat_tag", "disconnected")
                            trySend(ConnectionStatus.Disconnected)
                        }

                    }

                webSocketClient?.setConnectTimeout(CONNECTING_TIMEOUT)
                webSocketClient?.setReadTimeout(CONNECTION_LOST_TIMEOUT)
                webSocketClient?.enableAutomaticReconnection(WAIT_TIME_BEFORE_RECONNECTING)
                webSocketClient?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
                trySend(ConnectionStatus.Failed(e))
            }
            awaitClose()
        }.flowOn(coroutineDispatcher)
    }

    fun sendMessage(msg: String): Flow<Message> {
        webSocketClient?.send(msg)
        val message = Message(
            id = generateRandomId(),
            text = msg,
            dateTime = Date().time
        )
        Log.d("chat_tag", "sendMessage -> $msg")
        return emitFlow { message }.flowOn(coroutineDispatcher)
    }

    fun receiveMessage(): SharedFlow<Message> {
        return messageReceivedFlow
    }

    private fun generateRandomId(): String {
        return UUID.randomUUID().toString()
    }

    fun disconnect() {
        webSocketClient?.close(0, 0, "")
        webSocketClient = null
    }
}

private const val CONNECTING_TIMEOUT = 10 * 1000
private const val CONNECTION_LOST_TIMEOUT = 2 * 60 * 1000
private const val WAIT_TIME_BEFORE_RECONNECTING: Long = 2000