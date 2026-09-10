package com.ians.observer.domain.usecase.favorite

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkFavoriteArticlesUseCaseTest {

    @Test
    fun `marks articles listed in favorite urls`() = runTest {
        val favoriteArticle = testArticle(
            id = "favorite",
            originalUrl = "https://example.com/favorite",
        )
        val regularArticle = testArticle(
            id = "regular",
            originalUrl = "https://example.com/regular",
            isFavorite = true,
        )
        val articleRepository = FakeArticleRepository().apply {
            favoriteUrls.value = setOf(favoriteArticle.originalUrl)
        }
        val useCase = MarkFavoriteArticlesUseCase(articleRepository)

        val articles = useCase(flowOf(PagingData.from(listOf(favoriteArticle, regularArticle))))
            .take(1)
            .asSnapshot()

        assertTrue(articles.first { it.id == favoriteArticle.id }.isFavorite)
        assertFalse(articles.first { it.id == regularArticle.id }.isFavorite)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `favorite change re-emits without collecting upstream again`() = runTest {
        val article = testArticle(
            id = "article",
            originalUrl = "https://example.com/article",
        )
        val articleRepository = FakeArticleRepository()
        val useCase = MarkFavoriteArticlesUseCase(articleRepository)

        var upstreamCollections = 0
        val upstream = flow {
            upstreamCollections++
            emit(PagingData.from(listOf(article)))
        }

        val generations = mutableListOf<PagingData<Article>>()
        val collectionJob = launch {
            useCase(upstream).toList(generations)
        }

        runCurrent()
        articleRepository.favoriteUrls.value = setOf(article.originalUrl)
        runCurrent()
        collectionJob.cancel()

        assertEquals(1, upstreamCollections)
        assertEquals(2, generations.size)
        assertTrue(flowOf(generations.last()).asSnapshot().single().isFavorite)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `repeated favorite urls do not re-emit`() = runTest {
        val article = testArticle(originalUrl = "https://example.com/article")
        val articleRepository = FakeArticleRepository()
        val useCase = MarkFavoriteArticlesUseCase(articleRepository)

        val generations = mutableListOf<PagingData<Article>>()
        val collectionJob = launch {
            useCase(flowOf(PagingData.from(listOf(article)))).toList(generations)
        }

        runCurrent()
        articleRepository.favoriteUrls.value = emptySet()
        runCurrent()
        collectionJob.cancel()

        assertEquals(1, generations.size)
    }
}
