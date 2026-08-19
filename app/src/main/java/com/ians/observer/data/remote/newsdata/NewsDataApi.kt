package com.ians.observer.data.remote.newsdata

import retrofit2.http.GET
import retrofit2.http.Query

import com.ians.observer.data.remote.newsdata.dto.NewsDataResponse

interface NewsDataApi {
    @GET("1/latest")
    suspend fun getLatest(
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("page") page: String? = null,
        @Query("size") size: Int = 10,
        @Query("removeduplicate") removeDuplicate: Int = 1,
    ): NewsDataResponse

    @GET("1/latest")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("page") page: String? = null,
        @Query("size") size: Int = 10,
        @Query("removeduplicate") removeDuplicate: Int = 1,
    ): NewsDataResponse

    companion object {
        const val BASE_URL = "https://newsdata.io/api/"
    }
}
