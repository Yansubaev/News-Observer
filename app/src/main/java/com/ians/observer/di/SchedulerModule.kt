package com.ians.observer.di

import com.ians.observer.data.background.WorkManagerDailyNewsScheduler
import com.ians.observer.domain.background.DailyNewsScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    @Singleton
    abstract fun bindDailyNewsScheduler(
        workManagerDailyNewsScheduler: WorkManagerDailyNewsScheduler
    ): DailyNewsScheduler
}