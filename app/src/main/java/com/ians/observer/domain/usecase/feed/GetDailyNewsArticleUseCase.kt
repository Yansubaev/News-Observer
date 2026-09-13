package com.ians.observer.domain.usecase.feed

import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetDailyNewsArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(): Article? {
        val country = settingsRepository.observeCountryPreference().first()
        val lang = settingsRepository.observeLanguagePreference().first()
        val feedProviderId = settingsRepository.observeFeedProviderPreference().first()

        val spec = FeedSpec(
            country = country,
            language = lang,
            category = Category.GENERAL,
            providerIds = listOf(feedProviderId)
        )
        val articles = articleRepository.fetchTopHeadlines(spec)
        val randomArticle = articles.randomOrNull() ?: return null

        articleRepository.replaceNotificationArticle(randomArticle)

        return randomArticle
    }
}
