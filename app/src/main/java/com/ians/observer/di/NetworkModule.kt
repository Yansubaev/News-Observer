package com.ians.observer.di

import com.ians.observer.BuildConfig
import com.ians.observer.data.remote.newsapi.NewsApiApi
import com.ians.observer.data.remote.newsdata.NewsDataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @NewsApiNetwork
    fun provideNewsApiOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val url = originalRequest.url.newBuilder()
                    .addQueryParameter("apiKey", BuildConfig.NEWS_API_KEY)
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(url)
                    .build()

                chain.proceed(newRequest)
            }
            .build()
    }

    @Provides
    @Singleton
    @NewsDataNetwork
    fun provideNewsDataOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val url = originalRequest.url.newBuilder()
                    .addQueryParameter("apikey", BuildConfig.NEWS_DATA_API_KEY)
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(url)
                    .build()

                chain.proceed(newRequest)
            }
            .build()
    }

    @Provides
    @Singleton
    @NewsApiNetwork
    fun provideNewsApiRetrofit(@NewsApiNetwork okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NewsApiApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @NewsDataNetwork
    fun provideNewsDataRetrofit(@NewsDataNetwork okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NewsDataApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNewsApi(@NewsApiNetwork retrofit: Retrofit): NewsApiApi {
        return retrofit.create(NewsApiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsDataApi(@NewsDataNetwork retrofit: Retrofit): NewsDataApi {
        return retrofit.create(NewsDataApi::class.java)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsApiNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsDataNetwork
