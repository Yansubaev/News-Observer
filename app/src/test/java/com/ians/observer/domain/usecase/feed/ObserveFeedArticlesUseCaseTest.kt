package com.ians.observer.domain.usecase.feed

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.FakeSettingsRepository
import com.ians.observer.domain.usecase.fake.TopHeadlinesRequest
import com.ians.observer.domain.usecase.fake.testArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFeedArticlesUseCaseTest {

    @Test
    fun `builds feed spec from settings and returns repository articles`() = runTest {
        val article = testArticle(category = Category.SPORTS)
        val articleRepository = FakeArticleRepository().apply {
            topHeadlinesPagingFlow = kotlinx.coroutines.flow.flowOf(PagingData.from(listOf(article)))
        }
        val settingsRepository = FakeSettingsRepository(
            country = NewsCountry.RU,
            language = NewsLanguage.RU,
            feedProvider = ProviderId.NEWS_DATA,
            syncInterval = 42_000L,
        )
        val useCase = ObserveFeedArticlesUseCase(articleRepository, settingsRepository)

        val articles = useCase(Category.SPORTS).take(1).asSnapshot()

        assertEquals(listOf(article), articles)
        assertEquals(
            listOf(
                TopHeadlinesRequest(
                    spec = FeedSpec(
                        country = NewsCountry.RU,
                        language = NewsLanguage.RU,
                        category = Category.SPORTS,
                        providerIds = listOf(ProviderId.NEWS_DATA),
                    ),
                    syncInterval = 42_000L,
                )
            ),
            articleRepository.topHeadlinesRequests,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `requests a new feed when a setting changes`() = runTest {
        val articleRepository = FakeArticleRepository()
        val settingsRepository = FakeSettingsRepository(country = NewsCountry.US)
        val useCase = ObserveFeedArticlesUseCase(articleRepository, settingsRepository)
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase(Category.GENERAL).take(2).toList()
        }

        settingsRepository.countryPreference.value = NewsCountry.GB
        collectionJob.join()

        assertEquals(
            listOf(NewsCountry.US, NewsCountry.GB),
            articleRepository.topHeadlinesRequests.map { it.spec.country },
        )
    }
}
