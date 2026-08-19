package com.ians.observer.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ians.observer.data.local.converter.ListConverters

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "publisher_id")
    val publisherId: String?,

    @ColumnInfo(name = "publisher_name")
    val publisherName: String,

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

    @ColumnInfo(name = "content")
    val content: String?,
)
