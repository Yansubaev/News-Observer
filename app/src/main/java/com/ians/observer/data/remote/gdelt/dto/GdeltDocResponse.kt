package com.ians.observer.data.remote.gdelt.dto

import com.google.gson.annotations.SerializedName

data class GdeltDocResponse(
    @SerializedName("articles")
    val articles: List<GdeltArticleDto>?
)
