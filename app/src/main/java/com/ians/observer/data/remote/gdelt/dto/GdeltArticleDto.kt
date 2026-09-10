package com.ians.observer.data.remote.gdelt.dto

import com.google.gson.annotations.SerializedName

data class GdeltArticleDto(
    @SerializedName("url")
    val url: String,

    @SerializedName("url_mobile")
    val urlMobile: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("seendate")
    val seenDate: String,

    @SerializedName("socialimage")
    val socialImage: String?,

    @SerializedName("domain")
    val domain: String,

    @SerializedName("language")
    val language: String?,

    @SerializedName("sourcecountry")
    val sourceCountry: String?
)
