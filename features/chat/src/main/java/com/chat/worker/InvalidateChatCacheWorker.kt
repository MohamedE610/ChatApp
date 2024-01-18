package com.chat.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.chat.domain.interactor.ChatInterActor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class InvalidateChatCacheWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val interActor: ChatInterActor
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            interActor.invalidateCache()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        fun start(context: Context) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                InvalidateChatCacheWorker::class.java, 1, TimeUnit.DAYS
            ).addTag(InvalidateChatCacheWorker::class.java.simpleName).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    InvalidateChatCacheWorker::class.java.simpleName,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
        }
    }
}
