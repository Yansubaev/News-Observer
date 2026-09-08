package com.ians.observer.domain.usecase.search

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.FakeSettingsRepository
import com.ians.observer.domain.usecase.fake.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

class SearchArticlesUseCaseTest {

    @Test
    fun `blank query returns empty page without searching repository`() = runTest {
        val articleRepository = FakeArticleRepository()
        val useCase = SearchArticlesUseCase(
            articleRepository = articleRepository,
            settingsRepository = FakeSettingsRepository(),
        )

        val articles = useCase("   ").take(1).asSnapshot()

        assertTrue(articles.isEmpty())
        assertTrue(articleRepository.searchRequests.isEmpty())
    }

    @Test
    fun `builds search spec from settings and marks favorite articles`() = runTest {
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
            searchPagingFlow = flowOf(PagingData.from(listOf(favoriteArticle, regularArticle)))
            favoriteUrls.value = setOf(favoriteArticle.originalUrl)
        }
        val settingsRepository = FakeSettingsRepository(
            country = NewsCountry.GB,
            language = NewsLanguage.EN,
            searchProvider = ProviderId.NEWS_API,
        )
        val useCase = SearchArticlesUseCase(articleRepository, settingsRepository)

        val articles = useCase("android").take(1).asSnapshot()

        assertEquals(
            listOf(
                SearchSpec(
                    query = "android",
                    country = NewsCountry.GB,
                    language = NewsLanguage.EN,
                    category = null,
                    providerIds = listOf(ProviderId.NEWS_API),
                )
            ),
            articleRepository.searchRequests,
        )
        assertTrue(articles.first { it.id == favoriteArticle.id }.isFavorite)
        assertFalse(articles.first { it.id == regularArticle.id }.isFavorite)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `favorite change creates a new paging generation`() = runTest {
        val article = testArticle(
            id = "article",
            originalUrl = "https://example.com/article",
        )
        val articleRepository = FakeArticleRepository().apply {
            searchPagingFlowFactory = {
                flowOf(PagingData.from(listOf(article)))
            }
        }
        val useCase = SearchArticlesUseCase(
            articleRepository = articleRepository,
            settingsRepository = FakeSettingsRepository(),
        )

        val generations = mutableListOf<PagingData<Article>>()
        val collectionJob = launch {
            useCase("android").take(2).toList(generations)
        }

        runCurrent()
        articleRepository.favoriteUrls.value = setOf(article.originalUrl)
        collectionJob.join()

        val articles = flowOf(generations.last()).asSnapshot()

        assertEquals(2, articleRepository.searchRequests.size)
        assertTrue(articles.single().isFavorite)
    }
}
