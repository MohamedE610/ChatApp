package com.chat.data.di

import android.content.Context
import com.core.room.dao.MessagesDao
import com.core.room.database.MessagesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): MessagesDatabase {
        return MessagesDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRepoDao(database: MessagesDatabase): MessagesDao {
        return database.messageDao()
    }
}