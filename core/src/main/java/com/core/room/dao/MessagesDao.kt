package com.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.room.constant.RoomConstants
import com.core.room.entity.MessageEntity

@Dao
interface MessagesDao {
    @Query("SELECT * FROM ${RoomConstants.TABLE_NAME}")
    suspend fun getMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(msg: MessageEntity)

    @Query("DELETE FROM ${RoomConstants.TABLE_NAME} where id NOT IN (SELECT id from ${RoomConstants.TABLE_NAME} ORDER BY id ASC LIMIT 200)")
    suspend fun invalidateCache(): Int
}