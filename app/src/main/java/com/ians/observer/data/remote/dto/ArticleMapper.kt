package com.ians.observer.data.remote.dto

import com.ians.observer.domain.model.Article
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

fun ArticleDto.toArticle(isFavorite: Boolean = false, page: Int): Article {
    return Article(
        id = url,
        page = page,
        sourceId = source.id,
        sourceName = source.name,
        author = author,
        title = title,
        description = description,
        url = url,
        imageUrl = urlToImage,
        publishedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
            .parse(publishedAt)
            ?.time ?: System.currentTimeMillis(),
        content = content,
        isFavorite = isFavorite
    )
}