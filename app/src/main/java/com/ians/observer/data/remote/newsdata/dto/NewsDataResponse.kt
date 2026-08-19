package com.ians.observer.data.remote.newsdata.dto

import com.google.gson.annotations.SerializedName

data class NewsDataResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("totalResults")
    val totalResults: Int,

    @SerializedName("results")
    val results: List<NewsDataArticleDto>,

    @SerializedName("nextPage")
    val nextPage: String?
)