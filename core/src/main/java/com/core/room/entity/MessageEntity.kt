package com.core.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.core.room.constant.RoomConstants

@Entity(tableName = RoomConstants.TABLE_NAME)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo("id")
    val id: String,
    @ColumnInfo("text")
    val text: String,
    @ColumnInfo("dateTime")
    val dateTime: Long
)