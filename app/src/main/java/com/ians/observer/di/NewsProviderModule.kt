package com.ians.observer.di

import com.ians.observer.data.remote.gdelt.GdeltProvider
import com.ians.observer.data.remote.newsdata.NewsDataProvider
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.domain.model.ProviderId
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@MapKey
annotation class NewsProviderKey(val value: ProviderId)

@Module
@InstallIn(SingletonComponent::class)
abstract class NewsProviderModule {

    @Binds
    @Singleton
    @IntoMap
    @NewsProviderKey(ProviderId.NEWS_DATA)
    abstract fun bindNewsDataProvider(
        provider: NewsDataProvider
    ): NewsProvider

    @Binds
    @Singleton
    @IntoMap
    @NewsProviderKey(ProviderId.GDELT)
    abstract fun bindGdeltProvider(
        provider: GdeltProvider
    ): NewsProvider
}
