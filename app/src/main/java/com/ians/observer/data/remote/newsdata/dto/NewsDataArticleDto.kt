package com.ians.observer.data.remote.newsdata.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class NewsDataArticleDto(
    @SerializedName("article_id")
    val articleId: String,

    @SerializedName("link")
    val link: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("keywords")
    val keywords: List<String>?,

    @SerializedName("creator")
    val creator: List<String>?,

    @SerializedName("language")
    val language: String,

    @SerializedName("country")
    val country: List<String>,

    @SerializedName("category")
    val category: List<String>,

    @SerializedName("datatype")
    val datatype: String,

    @SerializedName("pubDate")
    val pubDate: String,

    @SerializedName("pubDateTZ")
    val pubDateTZ: String,

    @SerializedName("fetched_at")
    val fetchedAt: String,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("video_url")
    val videoUrl: String? = null,

    @SerializedName("source_id")
    val sourceId: String,

    @SerializedName("source_name")
    val sourceName: String,

    @SerializedName("source_priority")
    val sourcePriority: Int,

    @SerializedName("source_url")
    val sourceUrl: String,

    @SerializedName("source_icon")
    val sourceIcon: String?,

    @SerializedName("duplicate")
    val duplicate: Boolean
)