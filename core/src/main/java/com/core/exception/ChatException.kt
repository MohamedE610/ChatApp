package com.core.exception

import android.content.Context
import com.core.R
import java.io.IOException

sealed class ChatException : Throwable() {
    data object Business : ChatException()
    data object ServerDown : ChatException()
    data object ConnectionFailed : ChatException()
}

fun Throwable.toChatException(): ChatException {
    return try {
        when (this) {
            is IOException -> ChatException.Business
            else -> ChatException.ServerDown
        }
    } catch (e: Exception) {
        ChatException.ServerDown
    }
}

fun ChatException.getMessageShouldDisplay(
    ctx: Context,
    generalErrorMsgResId: Int = R.string.lbl_general_error_msg
): String {
    return ctx.getString(generalErrorMsgResId)
}