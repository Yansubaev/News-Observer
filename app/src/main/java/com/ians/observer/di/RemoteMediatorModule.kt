package com.ians.observer.di

import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.paging.ArticleRemoteMediatorFactory
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.domain.repository.SettingRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RemoteMediatorModule {

    fun provideArticleRemoteMediatorFactory(
        newsApi: NewsApi,
        database: NewsDatabase,
        settingRepository: SettingRepository
    ): ArticleRemoteMediatorFactory{
        return ArticleRemoteMediatorFactory(
            newsApi = newsApi,
            database = database,
            settingRepository = settingRepository
        )
    }
}