package com.core.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.core.room.constant.RoomConstants
import com.core.room.dao.MessagesDao
import com.core.room.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = RoomConstants.DB_VERSION,
    exportSchema = false,
)
abstract class MessagesDatabase : RoomDatabase() {

    abstract fun messageDao(): MessagesDao

    companion object {
        @Volatile
        private var INSTANCE: MessagesDatabase? = null

        fun getInstance(context: Context): MessagesDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            MessagesDatabase::class.java,
            RoomConstants.DB_NAME
        ).fallbackToDestructiveMigration().build()
    }
}