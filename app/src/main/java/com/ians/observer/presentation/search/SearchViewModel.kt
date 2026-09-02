package com.ians.observer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.SearchSpec
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SearchHistoryRepository
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingRepository: SettingRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val newsProviderRegistry: NewsProviderRegistry,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _liveQuery = MutableStateFlow("")
    val liveQuery: StateFlow<String> = _liveQuery.asStateFlow()

    val searchHistory = liveQuery
        .combine(searchHistoryRepository.getSearchHistory()) { q, h ->
            if (q.isBlank()) h.takeLast(5)
            else h.filter { it.contains(q, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val searchPages =
        combine(
            query,
            settingRepository.observeCountryPreference(),
            settingRepository.observeLanguagePreference(),
            settingRepository.observeSearchProviderPreference(),
        ) { query, country, language, searchProvider ->
            val effectiveProviderId =
                searchProvider.takeIf { it in newsProviderRegistry.availableIds }
                    ?: ProviderId.NEWS_DATA
            SearchSpec(
                query = query,
                country = country,
                language = language,
                providerIds = listOf(effectiveProviderId),
                category = null
            )
        }
            .distinctUntilChanged()
            .debounce(300.milliseconds)
            .flatMapLatest { spec ->
                if (spec.query.isBlank()) flowOf(PagingData.empty())
                else articleRepository.searchNewsPaging(spec)
            }
            .cachedIn(viewModelScope)

    val searchResultArticles = combine(
        searchPages,
        articleRepository.observeFavoriteUrls()
    ) { pagingData, favoriteUrls ->
        pagingData.map { article ->
            article.copy(
                isFavorite = article.originalUrl in favoriteUrls
            )
        }
    }
        .cachedIn(viewModelScope)

    fun setQueryText(query: String) {

        _liveQuery.value = query
    }

    fun clearSearchQuery(query: String) = viewModelScope.launch {
        searchHistoryRepository.deleteSearchQuery(query)
    }

    fun searchNews(query: String) = viewModelScope.launch {
        if (query.isBlank()) return@launch

        _query.value = query
        searchHistoryRepository.saveSearchQuery(query)
    }

    fun setFavorite(article: Article, shouldBeFavorite: Boolean) = viewModelScope.launch {
        if (shouldBeFavorite) {
            articleRepository.addToFavorites(article, null)
        } else {
            articleRepository.removeFromFavorites(article.id)
        }
    }
}