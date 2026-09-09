package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ians.observer.data.local.converter.ListConverters

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "article_provider_id")
    val providerId: String,

    @ColumnInfo(name = "article_category")
    val category: String?,

    @ColumnInfo(name = "publisher_id")
    val publisherId: String?,

    @ColumnInfo(name = "publisher_name")
    val publisherName: String,

    @ColumnInfo(name = "publisher_url")
    val publisherWebsiteUrl: String,

    @field:TypeConverters(ListConverters::class)
    @ColumnInfo(name = "authors")
    val authors: Set<String>?,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "image_url")
    val imageUrl: String?,

    @ColumnInfo(name = "published_at")
    val publishedAt: Long,
)
