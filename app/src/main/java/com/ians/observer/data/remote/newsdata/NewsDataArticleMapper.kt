package com.ians.observer.data.remote.newsdata

import com.ians.observer.BuildConfig
import com.ians.observer.data.remote.newsdata.dto.NewsDataArticleDto
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.domain.model.ProviderId
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun NewsDataArticleDto.toRemoteArticle() =
    RemoteArticle(
        id = this.articleId,
        providerId = ProviderId.NEWS_DATA,
        publisherId = this.sourceId,
        publisherName = this.sourceName,
        publisherWebsiteUrl = this.sourceUrl,
        authors = this.creator?.toSet(),
        title = this.title,
        description = this.description,
        originalUrl = this.link,
        imageUrl = this.imageUrl.takeIf { BuildConfig.IMAGES_ENABLED },
        publishedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(this.pubDate)
            ?.time
            ?: System.currentTimeMillis(),
        category = null
    )