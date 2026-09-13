package com.ians.observer.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ians.observer.domain.exception.NewsException
import com.ians.observer.domain.exception.isTransient
import com.ians.observer.domain.repository.SettingsRepository
import com.ians.observer.domain.usecase.feed.GetDailyNewsArticleUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class DailyNewsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val getDailyNewsArticleUseCase: GetDailyNewsArticleUseCase,
    private val dailyArticleNotifier: DailyArticleNotifier,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = try {
        val notificationsEnabled =
            settingsRepository.observeNotificationPreference().firstOrNull() ?: false

        if (!notificationsEnabled) return Result.success()

        if (!applicationContext.canPostNotifications()) return Result.success()

        val article = getDailyNewsArticleUseCase() ?: return Result.success()
        dailyArticleNotifier.showArticle(article)

        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: NewsException) {
        if (e.isTransient) retryOrFinish() else Result.failure()
    }

    private fun retryOrFinish(): Result =
        if (runAttemptCount < MAX_RETRY_COUNT) {
            Result.retry()
        } else {
            Result.failure()
        }

    companion object {
        const val MAX_RETRY_COUNT = 3
    }
}

private fun Context.canPostNotifications(): Boolean {
    val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    return runtimePermissionGranted &&
            NotificationManagerCompat.from(this).areNotificationsEnabled()
}