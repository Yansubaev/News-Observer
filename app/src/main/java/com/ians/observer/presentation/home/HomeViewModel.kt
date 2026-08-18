package com.ians.observer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val settingsRepository: SettingRepository
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articles: Flow<PagingData<Article>> = _selectedCategoryState
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { category ->
            articleRepository.getTopHeadlinesPaging(
                category = category,
                country = NewsCountry.fromCode(settingsRepository.getCountryPreference()),
                language = NewsLanguage.fromCode(settingsRepository.getLanguagePreference()),
                syncInterval = settingsRepository.getSyncInterval()
            )
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PagingData.empty()
        )

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