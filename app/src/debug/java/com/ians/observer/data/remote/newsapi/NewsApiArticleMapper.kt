package com.ians.observer.data.remote.newsapi

import android.net.Uri
import androidx.core.net.toUri
import com.ians.observer.data.remote.newsapi.dto.NewsApiArticleDto
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.domain.model.ProviderId
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.contains

fun NewsApiArticleDto.toRemoteArticle(): RemoteArticle {
    return RemoteArticle(
        id = this.url,
        publisherId = this.source.id,
        publisherName = this.source.name,
        publisherWebsiteUrl = extractPublisherWebsiteUrl(this.url) ?: this.url,
        authors = this.author?.let { setOf(it) },
        title = this.title,
        description = this.description,
        originalUrl = this.url,
        imageUrl = this.urlToImage,
        publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
            .parse(this.publishedAt)
            ?.time ?: System.currentTimeMillis(),
        category = null,
        providerId = ProviderId.NEWS_API
    )
}

private fun extractPublisherWebsiteUrl(rawUrl: String): String? {
    val uri = parseArticleUri(rawUrl) ?: return null

    return uri.buildUpon()
        .encodedPath("")
        .clearQuery()
        .fragment(null)
        .build()
        .toString()
}

private fun parseArticleUri(rawUrl: String): Uri? {
    val value = rawUrl.trim()
    if (value.isEmpty()) return null

    val uri = value.toUri()
    val scheme = uri.scheme?.lowercase(Locale.ROOT)

    return if (scheme in setOf("http", "https") && !uri.host.isNullOrBlank()) uri else null
}
