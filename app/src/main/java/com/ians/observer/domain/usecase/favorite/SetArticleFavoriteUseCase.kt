package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import javax.inject.Inject

class SetArticleFavoriteUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(
        article: Article,
        shouldBeFavorite: Boolean,
        category: Category? = article.category
    ) {
        if (shouldBeFavorite) {
            articleRepository.addToFavorites(
                article = article,
                category = category
            )
        } else {
            articleRepository.removeFromFavorites(article.id)
        }
    }
}