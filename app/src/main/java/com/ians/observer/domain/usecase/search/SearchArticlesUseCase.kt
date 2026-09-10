package com.ians.observer.domain.usecase.search

import androidx.paging.PagingData
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(query: String): Flow<PagingData<Article>> =
        combine(
            settingsRepository.observeCountryPreference(),
            settingsRepository.observeLanguagePreference(),
            settingsRepository.observeSearchProviderPreference(),
        ) { country, language, searchProvider ->
            SearchSpec(
                query = query,
                country = country,
                language = language,
                providerIds = listOf(searchProvider),
                category = null
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { spec ->
                if (spec.query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    articleRepository.searchNewsPaging(spec)
                }
            }
}
