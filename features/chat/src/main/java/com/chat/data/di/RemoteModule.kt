package com.chat.data.di

import com.core.BuildConfig
import com.core.constant.IO_DISPATCHER_KEY
import com.core.constant.WEB_SOCKET_URL_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    @Named(WEB_SOCKET_URL_KEY)
    fun provideWebSocketUrl(): String {
        return BuildConfig.WEB_SOCKET_URL
    }

    @Provides
    @Singleton
    @Named(IO_DISPATCHER_KEY)
    fun provideIOCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}