package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoriteCategoriesUseCaseTest {

    @Test
    fun `adds all before stored favorite categories`() = runTest {
        val repository = FakeArticleRepository()
        repository.favoriteCategories.value = listOf(Category.BUSINESS, Category.SPORTS)
        val useCase = ObserveFavoriteCategoriesUseCase(repository)

        val categories = useCase().first()

        assertEquals(
            listOf(Category.ALL, Category.BUSINESS, Category.SPORTS),
            categories,
        )
    }

    @Test
    fun `returns all when repository has no favorite categories`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = ObserveFavoriteCategoriesUseCase(repository)

        val categories = useCase().first()

        assertEquals(listOf(Category.ALL), categories)
    }
}
