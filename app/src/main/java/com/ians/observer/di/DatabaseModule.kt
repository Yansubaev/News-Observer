package com.ians.observer.di

import android.content.Context
import androidx.room.Room
import com.ians.observer.data.local.dao.ArticleDao
import com.ians.observer.data.local.dao.ArticlePagingDao
import com.ians.observer.data.local.dao.NewsDatabase
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
}