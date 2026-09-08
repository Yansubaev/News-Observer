package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveFavoriteCategoriesUseCase  @Inject constructor(
    private val articleRepository: ArticleRepository,
)  {
    operator fun invoke() =
        articleRepository.observeFavoriteCategories().map { strings ->
            listOf(Category.ALL) + strings
        }
}