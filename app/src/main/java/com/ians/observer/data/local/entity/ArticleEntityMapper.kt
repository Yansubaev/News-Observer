package com.ians.observer.data.local.entity

import com.ians.observer.data.local.projection.FavoriteArticleProjection
import com.ians.observer.data.local.projection.FeedArticleProjection
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher

fun ArticleEntity.toArticle(
    isFavorite: Boolean,
    category: Category?,
    providerId: ProviderId
): Article {
    return Article(
        id = this.url,
        providerId = providerId,
        publisher = Publisher(
            name = this.publisherName,
            id = this.publisherId
        ),
        authors = this.authors,
        title = this.title,
        description = this.description,
        originalUrl = this.url,
        imageUrl = this.imageUrl,
        publishedAt = this.publishedAt,
        content = this.content,
        isFavorite = isFavorite,
        category = category
    )
}

fun FeedArticleProjection.toArticle(): Article = article.toArticle(
    isFavorite = this.isFavorite,
    category = this.category?.let { Category.fromValue(it) },
    providerId = ProviderId.fromValue(this.providerId)
)

fun FavoriteArticleProjection.toArticle() : Article = article.toArticle(
    isFavorite = true,
    category = this.category?.let { Category.fromValue(it) },
    providerId = ProviderId.fromValue(this.providerId)
)