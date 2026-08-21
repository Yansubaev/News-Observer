package com.ians.observer.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingRepository
) : ViewModel() {

    private data class FeedParams(
        val category: Category,
        val country: NewsCountry,
        val language: NewsLanguage,
    )

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articles: Flow<PagingData<Article>> =
        combine(
            _selectedCategoryState,
            settingsRepository.observeCountryPreference(),
            settingsRepository.observeLanguagePreference(),
        ) { category, country, language ->
            FeedParams(
                category = category,
                country = country,
                language = language
            )
        }
            .distinctUntilChanged()
            .flatMapLatest { params ->
                articleRepository.getTopHeadlinesPaging(
                    FeedSpec(
                        country = params.country,
                        language = params.language,
                        category = params.category,
                        providerIds = listOf(ProviderId.NEWS_API)
                    ),
                    syncInterval = settingsRepository.getSyncInterval()
                )
            }
            .cachedIn(viewModelScope)

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        if (article.isFavorite) {
            articleRepository.removeFromFavorites(article.originalUrl)
        } else {
            articleRepository.addToFavorites(article, _selectedCategoryState.value)
        }
    }
}