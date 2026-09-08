package com.ians.observer.domain.usecase.search

import androidx.paging.PagingData
import androidx.paging.map
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(query: String) =
        combine(
            settingsRepository.observeCountryPreference(),
            settingsRepository.observeLanguagePreference(),
            settingsRepository.observeSearchProviderPreference(),
            articleRepository.observeFavoriteUrls(),
        ) { country, language, searchProvider, favoriteUrls ->
            SearchParameters(
                spec = SearchSpec(
                    query = query,
                    country = country,
                    language = language,
                    providerIds = listOf(searchProvider),
                    category = null
                ),
                favoriteUrls = favoriteUrls
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { parameters ->
                if (parameters.spec.query.isBlank()) {
                    flowOf(PagingData.empty())
                } else {
                    articleRepository.searchNewsPaging(parameters.spec)
                        .map { pagingData ->
                            pagingData.map { article ->
                                article.copy(
                                    isFavorite = article.originalUrl in parameters.favoriteUrls
                                )
                            }
                        }
                }
            }
}

private data class SearchParameters(
    val spec: SearchSpec,
    val favoriteUrls: Set<String>,
)
