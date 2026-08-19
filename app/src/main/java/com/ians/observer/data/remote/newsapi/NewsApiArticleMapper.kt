package com.ians.observer.data.remote.newsapi

import com.ians.observer.data.remote.newsapi.dto.NewsApiArticleDto
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.domain.model.ProviderId
import java.text.SimpleDateFormat
import java.util.Locale

fun NewsApiArticleDto.toRemoteArticle(): RemoteArticle {
    return RemoteArticle(
        id = this.url,
        publisherId = this.source.id,
        publisherName = this.source.name,
        authors = this.author?.let { setOf(it) },
        title = this.title,
        description = this.description,
        originalUrl = this.url,
        imageUrl = this.urlToImage,
        publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
            .parse(this.publishedAt)
            ?.time ?: System.currentTimeMillis(),
        content = this.content,
        category = null,
        providerId = ProviderId.NEWS_API
    )
}