package com.example.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

@androidx.hilt.work.HiltWorker
class MahirWidgetWorker @dagger.assisted.AssistedInject constructor(
    @dagger.assisted.Assisted private val appContext: Context,
    @dagger.assisted.Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        MahirWidget().updateAll(applicationContext)
        return Result.success()
    }
}