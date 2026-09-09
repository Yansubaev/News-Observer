package com.ians.observer.domain.usecase.feed

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import com.ians.observer.domain.usecase.fake.FakeSettingsRepository
import com.ians.observer.domain.usecase.fake.testArticle
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDailyNewsArticleUseCaseTest {

    @Test
    fun `builds feed spec from settings and saves returned article`() = runTest {
        val article = testArticle(id = "daily")
        val articleRepository = FakeArticleRepository().apply {
            fetchedTopHeadlines = listOf(article)
        }
        val settingsRepository = FakeSettingsRepository(
            country = NewsCountry.RU,
            language = NewsLanguage.RU,
            feedProvider = ProviderId.NEWS_DATA,
        )
        val useCase = GetDailyNewsArticleUseCase(articleRepository, settingsRepository)

        val result = useCase()

        assertEquals(article, result)
        assertEquals(
            listOf(
                FeedSpec(
                    country = NewsCountry.RU,
                    language = NewsLanguage.RU,
                    category = Category.GENERAL,
                    providerIds = listOf(ProviderId.NEWS_DATA),
                )
            ),
            articleRepository.fetchedTopHeadlinesRequests,
        )
        assertEquals(listOf(article), articleRepository.notificationArticles)
    }

    @Test
    fun `empty response returns null and does not save article`() = runTest {
        val articleRepository = FakeArticleRepository().apply {
            fetchedTopHeadlines = emptyList()
        }
        val useCase = GetDailyNewsArticleUseCase(
            articleRepository = articleRepository,
            settingsRepository = FakeSettingsRepository(),
        )

        val result = useCase()

        assertNull(result)
        assertTrue(articleRepository.notificationArticles.isEmpty())
    }

    @Test
    fun `invalid provider configuration returns null`() = runTest {
        val articleRepository = FakeArticleRepository().apply {
            fetchTopHeadlinesException = IllegalArgumentException("Provider is unavailable")
        }
        val useCase = GetDailyNewsArticleUseCase(
            articleRepository = articleRepository,
            settingsRepository = FakeSettingsRepository(),
        )

        val result = useCase()

        assertNull(result)
        assertTrue(articleRepository.notificationArticles.isEmpty())
    }

    @Test
    fun `network error is propagated so worker can retry`() = runTest {
        val expectedException = IOException("No connection")
        val articleRepository = FakeArticleRepository().apply {
            fetchTopHeadlinesException = expectedException
        }
        val useCase = GetDailyNewsArticleUseCase(
            articleRepository = articleRepository,
            settingsRepository = FakeSettingsRepository(),
        )
        var actualException: IOException? = null

        try {
            useCase()
        } catch (exception: IOException) {
            actualException = exception
        }

        assertSame(expectedException, actualException)
        assertTrue(articleRepository.notificationArticles.isEmpty())
    }
}
