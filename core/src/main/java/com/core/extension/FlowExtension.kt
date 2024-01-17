package com.core.extension

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

inline fun <T> emitFlow(crossinline emitter: suspend () -> T): Flow<T> {
    return flow {
        emit(emitter())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T, R> Flow<T>.flatMapFlow(crossinline transform: suspend (value: T) -> Flow<R>): Flow<R> {
    return flatMapMerge { value ->
        transform(value)
    }
}