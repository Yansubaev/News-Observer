package com.ians.observer.data.remote.newsapi.dto

import com.google.gson.annotations.SerializedName

data class NewsApiArticleDto(
    @SerializedName("source")
    val source: NewsApiSourceDto,

    @SerializedName("author")
    val author: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("url")
    val url: String,

    @SerializedName("urlToImage")
    val urlToImage: String?,

    @SerializedName("publishedAt")
    val publishedAt: String,
)