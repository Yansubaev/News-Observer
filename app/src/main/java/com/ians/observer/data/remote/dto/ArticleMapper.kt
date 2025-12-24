package com.ians.observer.data.remote.dto

import com.ians.observer.domain.model.Article
import java.util.UUID

fun ArticleDto.toArticle(isFavorite: Boolean = false): Article {
    return Article(
        id = UUID.randomUUID().toString(),
        sourceId = source.id,
        sourceName = source.name,
        author = author,
        title = title,
        description = description,
        url = url,
        imageUrl = urlToImage,
        publishedAt = publishedAt,
        content = content,
        isFavorite = isFavorite
    )
}

fun List<ArticleDto>.toArticleList(): List<Article> {
    return map { it.toArticle() }
}