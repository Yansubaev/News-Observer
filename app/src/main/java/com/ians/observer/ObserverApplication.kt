package com.ians.observer

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ians.observer.domain.usecase.settings.DeleteStaleArticlesUseCase
import com.ians.observer.notification.DailyArticleNotifier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ObserverApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dailyArticleNotifier: DailyArticleNotifier

    @Inject
    lateinit var deleteStaleArticlesUseCase: DeleteStaleArticlesUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        dailyArticleNotifier.createChannel()
        applicationScope.launch { deleteStaleArticlesUseCase() }
    }
}
