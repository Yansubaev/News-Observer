package com.ians.observer.di

import com.ians.observer.BuildConfig
import com.ians.observer.data.remote.gdelt.GdeltApi
import com.ians.observer.data.remote.gdelt.GdeltThrottleInterceptor
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

    //region NewsData Network
    @Provides
    @Singleton
    @NewsDataNetwork
    fun provideNewsDataOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
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
    @NewsDataNetwork
    fun provideNewsDataRetrofit(@NewsDataNetwork okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(NewsDataApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNewsDataApi(@NewsDataNetwork retrofit: Retrofit): NewsDataApi {
        return retrofit.create(NewsDataApi::class.java)
    }
    //endregion NewsData Network

    //region GDELT Network
    @Provides
    @Singleton
    @GdeltNetwork
    fun provideGdeltOkHttpClient(
        throttleInterceptor: GdeltThrottleInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(throttleInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @GdeltNetwork
    fun provideGdeltRetrofit(@GdeltNetwork okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(GdeltApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGdeltApi(@GdeltNetwork retrofit: Retrofit): GdeltApi {
        return retrofit.create(GdeltApi::class.java)
    }
    //endregion GDELT Network
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsDataNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GdeltNetwork
