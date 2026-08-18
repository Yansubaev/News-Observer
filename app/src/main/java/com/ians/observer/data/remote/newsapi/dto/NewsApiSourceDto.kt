package com.ians.observer.data.remote.newsapi.dto

import com.google.gson.annotations.SerializedName

data class NewsApiSourceDto(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String
)