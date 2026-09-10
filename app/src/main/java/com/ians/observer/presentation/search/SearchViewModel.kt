package com.ians.observer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.NewsProvidersRepository
import com.ians.observer.domain.repository.SearchHistoryRepository
import com.ians.observer.domain.repository.SettingsRepository
import com.ians.observer.domain.usecase.favorite.MarkFavoriteArticlesUseCase
import com.ians.observer.domain.usecase.favorite.SetArticleFavoriteUseCase
import com.ians.observer.domain.usecase.search.ObserveSearchHistoryUseCase
import com.ians.observer.domain.usecase.search.SearchArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val setArticleFavoriteUseCase: SetArticleFavoriteUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase,
    private val markFavoriteArticlesUseCase: MarkFavoriteArticlesUseCase,
    private val observeSearchHistoryUseCase: ObserveSearchHistoryUseCase,

    private val searchHistoryRepository: SearchHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val newsProvidersRepository: NewsProvidersRepository,
) : ViewModel() {

    private val _queryState = MutableStateFlow("")
    val queryState: StateFlow<String> = _queryState.asStateFlow()

    private val _liveQuery = MutableStateFlow("")
    val liveQuery: StateFlow<String> = _liveQuery.asStateFlow()

    private val searchProviderIds = newsProvidersRepository.getSearchCapableProviderIds()

    val enabledSearchProviderIds = settingsRepository.observeEnabledSearchProviderIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = searchProviderIds
        )

    val selectedSearchProvider = settingsRepository.observeSearchProviderPreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProviderId.NEWS_DATA
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchHistory = liveQuery
        .flatMapLatest(observeSearchHistoryUseCase::invoke)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Whether a search has actually been requested. Paging load states cannot tell "nothing typed
     * yet" apart from "nothing found", so the distinction is kept here instead.
     */
    val hasQuery: StateFlow<Boolean> = queryState
        .map { query -> query.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResultArticles = markFavoriteArticlesUseCase(
        queryState
            .flatMapLatest(searchArticlesUseCase::invoke)
            .cachedIn(viewModelScope)
    )

    fun setQueryText(query: String) {
        _liveQuery.value = query
    }

    fun clearSearchQuery(query: String) = viewModelScope.launch {
        searchHistoryRepository.deleteSearchQuery(query)
    }

    fun searchNews(query: String) = viewModelScope.launch {
        if (query.isBlank()) return@launch

        _queryState.value = query
        searchHistoryRepository.saveSearchQuery(query)
    }

    fun selectSearchProvider(providerId: ProviderId) = viewModelScope.launch {
        settingsRepository.setSearchProviderPreference(providerId)
    }

    fun setFavorite(article: Article, shouldBeFavorite: Boolean) = viewModelScope.launch {
        setArticleFavoriteUseCase(
            article = article,
            shouldBeFavorite = shouldBeFavorite,
            category = null
        )
    }
}
