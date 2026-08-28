package com.ians.observer.data.local.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ians.observer.data.local.converter.ListConverters
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.data.local.entity.ArticleFeedCrossRefEntity
import com.ians.observer.data.local.entity.FavoriteEntity
import com.ians.observer.data.local.entity.FeedEntity
import com.ians.observer.data.local.entity.RemoteKeyEntity

@Database(
    entities = [
        ArticleEntity::class,
        RemoteKeyEntity::class,
        ArticleFeedCrossRefEntity::class,
        FavoriteEntity::class,
        FeedEntity::class
    ],
    version = 12,
    exportSchema = false,
)
@TypeConverters(ListConverters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun articlePagingDao(): ArticlePagingDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun articleFeedCrossRefDao(): ArticleFeedCrossRefDao
    abstract fun feedDao(): FeedDao
}
