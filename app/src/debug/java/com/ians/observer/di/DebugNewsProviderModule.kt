package com.ians.observer.di

import com.ians.observer.data.remote.newsapi.NewsApiProvider
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.domain.model.ProviderId
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugNewsProviderModule {

    @Binds
    @Singleton
    @IntoMap
    @NewsProviderKey(ProviderId.NEWS_API)
    abstract fun bindNewsApiProvider(
        provider: NewsApiProvider
    ): NewsProvider

}