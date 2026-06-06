package com.ians.observer.presentation.home

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.data.local.entity.categoryFromString
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.listOf

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val articles: Flow<PagingData<Article>> = _selectedCategoryState
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { category ->
            articleRepository.getTopHeadlinesPaging(country = "us", category = category.value)
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PagingData.empty()
        )


    init {
        _selectedCategoryState.value = Category.GENERAL
    }

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        articleRepository.toggleFavorite(article)
    }
}