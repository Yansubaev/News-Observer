package com.ians.observer.domain.usecase.favorite

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.testArticle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetArticleFavoriteUseCaseTest {

    @Test
    fun `adds article to favorites with explicit category`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = SetArticleFavoriteUseCase(repository)
        val article = testArticle(category = Category.GENERAL)

        useCase(
            article = article,
            shouldBeFavorite = true,
            category = Category.SPORTS,
        )

        assertEquals(listOf(article to Category.SPORTS), repository.addedFavorites)
        assertTrue(repository.removedFavoriteIds.isEmpty())
    }

    @Test
    fun `uses article category when category argument is omitted`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = SetArticleFavoriteUseCase(repository)
        val article = testArticle(category = Category.TECHNOLOGY)

        useCase(article = article, shouldBeFavorite = true)

        assertEquals(listOf(article to Category.TECHNOLOGY), repository.addedFavorites)
    }

    @Test
    fun `removes article from favorites by id`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = SetArticleFavoriteUseCase(repository)
        val article = testArticle(id = "removed-article")

        useCase(article = article, shouldBeFavorite = false)

        assertEquals(listOf("removed-article"), repository.removedFavoriteIds)
        assertTrue(repository.addedFavorites.isEmpty())
    }
}
