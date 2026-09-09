package com.ians.observer.di

import android.content.Context
import androidx.room.Room
import com.ians.observer.data.local.dao.ArticleDao
import com.ians.observer.data.local.dao.ArticlePagingDao
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.local.dao.RemoteKeyDao
import com.ians.observer.data.local.migration.MIGRATION_12_13
import com.ians.observer.data.local.migration.MIGRATION_6_7
import com.ians.observer.data.local.migration.MIGRATION_8_9
import com.ians.observer.data.local.migration.MIGRATION_9_10
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNewsDatabase(
        @ApplicationContext context: Context
    ): NewsDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = NewsDatabase::class.java,
            name = "news_database"
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(
                MIGRATION_6_7,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_12_13,
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(
        database: NewsDatabase
    ): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideArticlePagingSourceDao(
        database: NewsDatabase
    ): ArticlePagingDao {
        return database.articlePagingDao()
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(
        database: NewsDatabase
    ): RemoteKeyDao {
        return database.remoteKeyDao()
    }
}
