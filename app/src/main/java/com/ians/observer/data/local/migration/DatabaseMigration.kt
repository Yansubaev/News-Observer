package com.ians.observer.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TEMP TABLE migration_8_9_article_feed_cross_refs AS
            SELECT feed_key, provider_id, article_url, position
            FROM article_feed_cross_refs
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TEMP TABLE migration_8_9_favorites AS
            SELECT article_url, saved_at, category, source_provider_id
            FROM favorites
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE article_feed_cross_refs")
        db.execSQL("DROP TABLE favorites")

        db.execSQL(
            """
            CREATE TABLE articles_new (
                url TEXT NOT NULL,
                publisher_id TEXT,
                publisher_name TEXT NOT NULL,
                authors TEXT,
                title TEXT NOT NULL,
                description TEXT,
                image_url TEXT,
                published_at INTEGER NOT NULL,
                content TEXT,
                PRIMARY KEY(url)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO articles_new (
                url,
                publisher_id,
                publisher_name,
                authors,
                title,
                description,
                image_url,
                published_at,
                content
            )
            SELECT
                url,
                publisher_id,
                publisher_name,
                NULL,
                title,
                description,
                image_url,
                published_at,
                content
            FROM articles
            """.trimIndent(),
        )

        val gson = Gson()
        db.compileStatement(
            "UPDATE articles_new SET authors = ? WHERE url = ?",
        ).use { updateAuthorsStatement ->
            db.query(
                "SELECT url, author FROM articles WHERE author IS NOT NULL",
            ).use { cursor ->
                val urlColumnIndex = cursor.getColumnIndexOrThrow("url")
                val authorColumnIndex = cursor.getColumnIndexOrThrow("author")

                while (cursor.moveToNext()) {
                    updateAuthorsStatement.bindString(
                        1,
                        gson.toJson(setOf(cursor.getString(authorColumnIndex))),
                    )
                    updateAuthorsStatement.bindString(2, cursor.getString(urlColumnIndex))
                    updateAuthorsStatement.executeUpdateDelete()
                    updateAuthorsStatement.clearBindings()
                }
            }
        }

        db.execSQL("DROP TABLE articles")
        db.execSQL("ALTER TABLE articles_new RENAME TO articles")

        db.execSQL(
            """
            CREATE TABLE article_feed_cross_refs (
                feed_key TEXT NOT NULL,
                provider_id TEXT NOT NULL,
                article_url TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(feed_key, provider_id, article_url),
                FOREIGN KEY(article_url) REFERENCES articles(url)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(feed_key, provider_id) REFERENCES feeds(feed_key, provider_id)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX index_article_feed_cross_refs_article_url
            ON article_feed_cross_refs(article_url)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX index_article_feed_cross_refs_feed_key_provider_id
            ON article_feed_cross_refs(feed_key, provider_id)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX index_article_feed_cross_refs_feed_key_provider_id_position
            ON article_feed_cross_refs(feed_key, provider_id, position)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO article_feed_cross_refs (feed_key, provider_id, article_url, position)
            SELECT feed_key, provider_id, article_url, position
            FROM migration_8_9_article_feed_cross_refs
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE favorites (
                article_url TEXT NOT NULL,
                saved_at INTEGER NOT NULL,
                category TEXT,
                source_provider_id TEXT NOT NULL,
                PRIMARY KEY(article_url),
                FOREIGN KEY(article_url) REFERENCES articles(url)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX index_favorites_article_url
            ON favorites(article_url)
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO favorites (article_url, saved_at, category, source_provider_id)
            SELECT article_url, saved_at, category, source_provider_id
            FROM migration_8_9_favorites
            """.trimIndent(),
        )

        db.execSQL("DROP TABLE migration_8_9_article_feed_cross_refs")
        db.execSQL("DROP TABLE migration_8_9_favorites")
    }
}

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
