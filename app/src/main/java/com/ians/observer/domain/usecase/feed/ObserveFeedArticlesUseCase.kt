package com.ians.observer.domain.usecase.feed

import androidx.paging.PagingData
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class ObserveFeedArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        category: Category
    ): Flow<PagingData<Article>> =
        combine(
            settingsRepository.observeCountryPreference(),
            settingsRepository.observeLanguagePreference(),
            settingsRepository.observeFeedProviderPreference(),
        ) { country, lang, provider ->
            FeedSpec(
                country = country,
                language = lang,
                category = category,
                providerIds = listOf(provider)
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { spec ->
                articleRepository.observeTopHeadlinesPaging(
                    spec = spec,
                    syncInterval = settingsRepository.getSyncInterval()
                )
            }
}