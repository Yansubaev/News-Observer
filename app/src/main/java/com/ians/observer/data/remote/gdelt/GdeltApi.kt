package com.ians.observer.data.remote.gdelt

import com.ians.observer.data.remote.gdelt.dto.GdeltDocResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GdeltApi {
    @GET("v2/doc/doc")
    suspend fun searchArticles(
        @Query("query") query: String,
        @Query("maxrecords") maxRecords: Int = 250,
        @Query("sort") sort: String = "datedesc",
        @Query("mode") mode: String = "artlist",
        @Query("format") format: String = "json",
    ): GdeltDocResponse?

    companion object {
        const val BASE_URL = "https://api.gdeltproject.org/api/"
    }
}
