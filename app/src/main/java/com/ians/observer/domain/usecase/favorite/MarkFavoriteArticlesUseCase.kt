package com.ians.observer.domain.usecase.favorite

import androidx.paging.PagingData
import androidx.paging.map
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class MarkFavoriteArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    operator fun invoke(articles: Flow<PagingData<Article>>): Flow<PagingData<Article>> =
        articles.combine(
            articleRepository.observeFavoriteUrls().distinctUntilChanged()
        ) { pagingData, favoriteUrls ->
            pagingData.map { article ->
                article.copy(isFavorite = article.originalUrl in favoriteUrls)
            }
        }
}
