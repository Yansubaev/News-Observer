package com.ians.observer.di

import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.data.repository.ArticleRepositoryImpl
import com.ians.observer.data.repository.SettingsRepositoryImpl
import com.ians.observer.domain.repository.SettingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        articleRepositoryImpl: ArticleRepositoryImpl
    ): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindSettingRepository(
        settingRepositoryImpl: SettingsRepositoryImpl
    ): SettingRepository

}