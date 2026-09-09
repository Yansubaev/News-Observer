package com.ians.observer.domain.usecase.feed

import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetDailyNewsArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(): Article? =
        try {
            val country = settingsRepository.observeCountryPreference().firstOrNull()
                ?: NewsCountry.US

            val lang = settingsRepository.observeLanguagePreference().firstOrNull()
                ?: NewsLanguage.EN

            val feedProviderId = settingsRepository.observeFeedProviderPreference().firstOrNull()
                ?: return null

            val spec = FeedSpec(
                country = country,
                language = lang,
                category = Category.GENERAL,
                providerIds = listOf(feedProviderId)
            )
            val articles = articleRepository.fetchTopHeadlines(spec)
            val randomArticle = articles.randomOrNull() ?: return null

            articleRepository.replaceNotificationArticle(randomArticle)

            randomArticle
        } catch (e: IllegalArgumentException) {
            null
        }

}
