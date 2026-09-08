package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import javax.inject.Inject

class ObserveFavoriteArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    operator fun invoke(category: Category) =
        articleRepository.observeFavoriteArticlesForCategory(category)
}