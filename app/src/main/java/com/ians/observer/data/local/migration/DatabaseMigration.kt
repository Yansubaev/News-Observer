package com.ians.observer.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
              CREATE TABLE articles_new (
                  url TEXT NOT NULL,
                  source_id TEXT,
                  source_name TEXT NOT NULL,
                  author TEXT,
                  title TEXT NOT NULL,
                  description TEXT,
                  image_url TEXT,
                  published_at INTEGER NOT NULL,
                  content TEXT,
                  is_favorite INTEGER NOT NULL,
                  saved_at INTEGER NOT NULL,
                  category TEXT,
                  PRIMARY KEY(url)
              )
              """.trimIndent(),
        )

        db.execSQL(
            """
              INSERT INTO articles_new (
                  url,
                  source_id,
                  source_name,
                  author,
                  title,
                  description,
                  image_url,
                  published_at,
                  content,
                  is_favorite,
                  saved_at,
                  category
              )
              SELECT
                  url,
                  source_id,
                  source_name,
                  author,
                  title,
                  description,
                  image_url,
                  published_at,
                  content,
                  is_favorite,
                  saved_at,
                  category
              FROM articles
              """.trimIndent(),
        )

        db.execSQL("DROP TABLE articles")
        db.execSQL("ALTER TABLE articles_new RENAME TO articles")

        db.execSQL(
            """
              CREATE TABLE remote_keys (
                  feed_key TEXT NOT NULL,
                  provider_id TEXT NOT NULL,
                  next_page_token TEXT,
                  end_reached INTEGER NOT NULL,
                  updated_at INTEGER NOT NULL,
                  PRIMARY KEY(feed_key, provider_id)
              )
              """.trimIndent(),
        )
    }
}