package com.ians.observer.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.usecase.favorite.SetArticleFavoriteUseCase
import com.ians.observer.domain.usecase.feed.ObserveAvailableCategoriesUseCase
import com.ians.observer.domain.usecase.feed.ObserveFeedArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val observeFeedArticlesUseCase: ObserveFeedArticlesUseCase,
    private val observeAvailableCategoriesUseCase: ObserveAvailableCategoriesUseCase,
    private val setArticleFavoriteUseCase: SetArticleFavoriteUseCase,
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    val availableCategories: Flow<Set<Category>> = observeAvailableCategoriesUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articles: Flow<PagingData<Article>> = selectedCategoryState
            .flatMapLatest(observeFeedArticlesUseCase::invoke)
            .cachedIn(viewModelScope)

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        setArticleFavoriteUseCase(
            article = article,
            shouldBeFavorite = !article.isFavorite,
            category = _selectedCategoryState.value
        )
    }
}