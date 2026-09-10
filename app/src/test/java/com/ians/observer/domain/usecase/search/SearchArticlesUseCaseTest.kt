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
    fun `builds search spec from settings`() = runTest {
        val articleRepository = FakeArticleRepository().apply {
            searchPagingFlow = flowOf(PagingData.from(listOf(testArticle())))
        }
        val settingsRepository = FakeSettingsRepository(
            country = NewsCountry.GB,
            language = NewsLanguage.EN,
            searchProvider = ProviderId.NEWS_API,
        )
        val useCase = SearchArticlesUseCase(articleRepository, settingsRepository)

        useCase("android").take(1).asSnapshot()

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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selected provider change restarts the submitted search`() = runTest {
        val articleRepository = FakeArticleRepository().apply {
            searchPagingFlowFactory = { flowOf(PagingData.empty()) }
        }
        val settingsRepository = FakeSettingsRepository(searchProvider = ProviderId.NEWS_DATA)
        val useCase = SearchArticlesUseCase(articleRepository, settingsRepository)

        val collectionJob = launch {
            useCase("android").take(2).toList()
        }

        runCurrent()
        settingsRepository.setSearchProviderPreference(ProviderId.NEWS_API)
        collectionJob.join()

        assertEquals(
            listOf(ProviderId.NEWS_DATA, ProviderId.NEWS_API),
            articleRepository.searchRequests.map { it.providerIds.single() }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `favorite change does not restart the search`() = runTest {
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
            useCase("android").toList(generations)
        }

        runCurrent()
        articleRepository.favoriteUrls.value = setOf(article.originalUrl)
        runCurrent()
        collectionJob.cancel()

        assertEquals(1, articleRepository.searchRequests.size)
        assertEquals(1, generations.size)
    }
}
