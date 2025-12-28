package com.ians.observer.data.local.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ians.observer.data.local.entity.ArticleEntity

@Database(
    entities = [ArticleEntity::class],
    version = 5,
    exportSchema = false
)

abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun articlePagingDao(): ArticlePagingDao
}