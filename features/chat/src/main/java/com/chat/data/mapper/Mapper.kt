package com.chat.data.mapper

import com.chat.entity.Message
import com.core.room.entity.MessageEntity

fun MessageEntity.map(): Message {
    return Message(
        id = id,
        text = text,
        dateTime = dateTime
    )
}

fun Message.map(): MessageEntity {
    return MessageEntity(
        id = id,
        text = text,
        dateTime = dateTime
    )
}