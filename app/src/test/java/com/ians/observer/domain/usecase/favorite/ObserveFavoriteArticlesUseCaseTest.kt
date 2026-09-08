package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.testArticle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoriteArticlesUseCaseTest {

    @Test
    fun `returns favorite articles for requested category`() = runTest {
        val repository = FakeArticleRepository()
        val expectedArticles = listOf(testArticle(category = Category.SPORTS))
        repository.favoriteArticlesByCategory[Category.SPORTS] = flowOf(expectedArticles)
        val useCase = ObserveFavoriteArticlesUseCase(repository)

        val actualArticles = useCase(Category.SPORTS).first()

        assertEquals(expectedArticles, actualArticles)
        assertEquals(listOf(Category.SPORTS), repository.requestedFavoriteCategories)
    }

    @Test
    fun `passes synthetic all category to repository`() = runTest {
        val repository = FakeArticleRepository()
        val expectedArticles = listOf(testArticle())
        repository.favoriteArticlesByCategory[Category.ALL] = flowOf(expectedArticles)
        val useCase = ObserveFavoriteArticlesUseCase(repository)

        val actualArticles = useCase(Category.ALL).first()

        assertEquals(expectedArticles, actualArticles)
        assertEquals(listOf(Category.ALL), repository.requestedFavoriteCategories)
    }
}
