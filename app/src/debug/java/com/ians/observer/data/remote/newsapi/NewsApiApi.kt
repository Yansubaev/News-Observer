package com.ians.observer.data.remote.newsapi

import com.ians.observer.data.remote.newsapi.dto.NewsApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiApi {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): NewsApiResponse

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("language") language: String? = "en",
    ): NewsApiResponse

    companion object{
        const val BASE_URL = "https://newsapi.org/"
    }
}