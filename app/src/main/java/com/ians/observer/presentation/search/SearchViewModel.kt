package com.ians.observer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val _query = MutableStateFlow<String>("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _liveQuery = MutableStateFlow<String>("")
    val liveQuery: StateFlow<String> = _liveQuery.asStateFlow()

    val searchHistory = liveQuery
        .combine(settingRepository.getSearchHistory()) { q, h ->
            if (q.isBlank()) h.takeLast(5)
            else h.filter { it.contains(q, ignoreCase = true) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResultArticles = query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(PagingData.empty())
            else articleRepository.searchNewsPaging(q, "en")
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PagingData.empty()
        )

    fun setQueryText(query: String) {

        _liveQuery.value = query
    }

    fun clearSearchQuery(query: String) = viewModelScope.launch {
        settingRepository.deleteSearchQuery(query)
    }

    fun searchNews(query: String) = viewModelScope.launch {
        if (query.isBlank()) return@launch

        _query.value = query
        settingRepository.saveSearchQuery(query)
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        articleRepository.toggleFavorite(article)
    }
}