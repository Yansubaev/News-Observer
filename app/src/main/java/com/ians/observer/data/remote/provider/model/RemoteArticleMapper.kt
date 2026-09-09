package com.ians.observer.data.remote.provider.model

import com.ians.observer.BuildConfig
import com.ians.observer.data.local.entity.ArticleEntity
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Publisher

fun RemoteArticle.toEntity(): ArticleEntity =
    ArticleEntity(
        id = this.id,
        url = this.originalUrl,
        providerId = this.providerId.value,
        category = this.category?.value,
        publisherId = this.publisherId,
        publisherName = this.publisherName,
        publisherWebsiteUrl = this.publisherWebsiteUrl,
        authors = this.authors,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl.takeIf { BuildConfig.IMAGES_ENABLED },
        publishedAt = this.publishedAt,
    )

fun RemoteArticle.toArticle(isFavorite: Boolean): Article = Article(
    id = this.id,
    publisher = Publisher(
        name = this.publisherName,
        id = this.publisherId,
        websiteUrl = this.publisherWebsiteUrl,
    ),
    authors = this.authors,
    title = this.title,
    description = this.description,
    originalUrl = this.originalUrl,
    imageUrl = this.imageUrl.takeIf { BuildConfig.IMAGES_ENABLED },
    publishedAt = this.publishedAt,
    isFavorite = isFavorite,
    category = category,
    providerId = providerId
)
