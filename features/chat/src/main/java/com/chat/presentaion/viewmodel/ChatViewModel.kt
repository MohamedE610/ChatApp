package com.chat.presentaion.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.domain.interactor.ChatInterActor
import com.chat.entity.ConnectionStatus
import com.chat.entity.Message
import com.core.exception.ChatException
import com.core.exception.toChatException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val interActor: ChatInterActor
) : ViewModel() {
    private val _screenState: MutableStateFlow<ChatState> by lazy { MutableStateFlow(ChatState.Initial) }
    val screenState: StateFlow<ChatState> = _screenState
    private var connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected
    private val messages = arrayListOf<Message>()

    init {
        connect()
    }

    private fun connect() {
        _screenState.value = ChatState.Loading
        viewModelScope.launch {
            interActor.connect()
                .catch { ChatState.Error(it.toChatException()) }
                .collect {
                    Log.d("chat_tag", "UI:$it")
                    connectionStatus = it
                    if (it is ConnectionStatus.Connected) {
                        loadHistory()
                        receiveMessage()
                    } else {
                        _screenState.value = ChatState.Error(ChatException.ConnectionFailed)
                    }
                }
        }
    }

    private fun loadHistory() {
        _screenState.value = ChatState.Loading
        viewModelScope.launch {
            interActor.loadHistory()
                .catch { ChatState.Error(it.toChatException()) }
                .collect {
                    cacheMessages(it)
                    if (it.isEmpty())
                        _screenState.value = ChatState.NoHistoryCached
                    else
                        _screenState.value = ChatState.HistoryLoaded(it.reversed())
                }
        }
    }

    fun sendMessage(msg: String) {
        viewModelScope.launch {
            interActor.sendMessage(msg)
                .catch { ChatState.Error(it.toChatException()) }
                .collect {
                    cacheMessage(it)
                    _screenState.value = ChatState.MessageSent(messages.reversed())
                }
        }
    }

    private fun receiveMessage() {
        viewModelScope.launch {
            interActor.receiveMessage()
                .catch { ChatState.Error(it.toChatException()) }
                .collect {
                    cacheMessage(it)
                    _screenState.value = ChatState.MessageReceived(messages.reversed())
                }
        }
    }

    private fun cacheMessages(it: List<Message>) {
        messages.clear()
        messages.addAll(it)
    }

    private fun cacheMessage(it: Message) {
        messages.add(it)
    }

    override fun onCleared() {
        viewModelScope.launch { interActor.disconnect() }
        super.onCleared()
    }
}

sealed class ChatState {
    data object Initial : ChatState()
    data object Loading : ChatState()
    data class HistoryLoaded(val date: List<Message>) : ChatState()
    data object NoHistoryCached : ChatState()
    class MessageSent(val date: List<Message>) : ChatState()
    class MessageReceived(val date: List<Message>) : ChatState()
    data class Error(val ex: ChatException) : ChatState()
}