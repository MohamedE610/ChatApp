package com.chat.data.source.remote

import android.util.Log
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import com.core.constant.IO_DISPATCHER_KEY
import com.core.constant.WEB_SOCKET_URL_KEY
import com.core.extension.emitFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ChatRemoteDataSource @Inject constructor(
    @Named(WEB_SOCKET_URL_KEY) private val url: String,
    @Named(IO_DISPATCHER_KEY) private val coroutineDispatcher: CoroutineDispatcher
) {

    private var webSocket: WebSocketClient? = null
    private val messageReceivedFlow = MutableSharedFlow<Message>(replay = 1)

    fun connect(): Flow<ConnectionStatus> {
        return callbackFlow {
            try {
                webSocket = object : WebSocketClient(URI(url)) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        Log.d("chat_tag", "connected")
                        trySend(ConnectionStatus.Connected)
                    }

                    override fun onMessage(message: String?) {
                        Log.d("chat_tag", "message received -> $message")
                        val msg = Message(
                            id = generateRandomId(),
                            text = message ?: "",
                            dateTime = Date().time
                        )
                        messageReceivedFlow.tryEmit(msg)
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        Log.d("chat_tag", "disconnected")
                        trySend(ConnectionStatus.Disconnected)
                        cancel()
                    }

                    override fun onError(ex: Exception?) {
                        Log.d("chat_tag", "failed, ex-> ${ex?.message}")
                        trySend(ConnectionStatus.Failed(ex ?: return))
                        cancel()
                    }

                }
                webSocket?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
                trySend(ConnectionStatus.Failed(e))
                cancel()
            }
            awaitClose()
        }.flowOn(coroutineDispatcher)
    }

    fun sendMessage(msg: String): Flow<Message> {
        webSocket?.send(msg)
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
        webSocket?.close()
        webSocket = null
    }
}