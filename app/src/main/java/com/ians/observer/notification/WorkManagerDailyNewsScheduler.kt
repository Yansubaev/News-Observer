package com.ians.observer.notification

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ians.observer.domain.background.DailyNewsScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerDailyNewsScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : DailyNewsScheduler {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<DailyNewsWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(networkConstraints())
            .setInitialDelay(
                duration = 1,
                timeUnit = TimeUnit.DAYS
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 30,
                timeUnit = TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = request,
            )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    override fun enqueueOneTime() {
        val request = OneTimeWorkRequestBuilder<DailyNewsWorker>()
            .setConstraints(networkConstraints())
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 10,
                timeUnit = TimeUnit.SECONDS,
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                TEST_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
    }

    private fun networkConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        const val WORK_NAME = "daily_news_notification"
        const val TEST_WORK_NAME = "test_daily_news_notification"
    }
}
