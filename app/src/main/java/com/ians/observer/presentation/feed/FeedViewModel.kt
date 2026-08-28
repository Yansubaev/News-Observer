package com.ians.observer.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.data.remote.provider.NewsProviderRegistry
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.FeedSpec
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.repository.ArticleRepository
import com.ians.observer.domain.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingRepository,
    private val newsProviderRegistry: NewsProviderRegistry
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    val availableCategories: Flow<Set<Category>> =
        settingsRepository.observeFeedProviderPreference()
            .map { providerId ->
                newsProviderRegistry
                    .require(providerId)
                    .supportedCategories
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articles: Flow<PagingData<Article>> =
        combine(
            _selectedCategoryState,
            settingsRepository.observeCountryPreference(),
            settingsRepository.observeLanguagePreference(),
            settingsRepository.observeFeedProviderPreference(),
        ) { category, country, language, feedProvider ->
            FeedSpec(
                category = category,
                country = country,
                language = language,
                providerIds = listOf(feedProvider)
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { spec ->
                articleRepository.getTopHeadlinesPaging(
                    spec = spec,
                    syncInterval = settingsRepository.getSyncInterval()
                )
            }
            .cachedIn(viewModelScope)

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        if (article.isFavorite) {
            articleRepository.removeFromFavorites(article.id)
        } else {
            articleRepository.addToFavorites(article, _selectedCategoryState.value)
        }
    }
}