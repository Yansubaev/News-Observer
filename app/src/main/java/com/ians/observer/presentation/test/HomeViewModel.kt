package com.ians.observer.presentation.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<PagingData<Article>> = _selectedCategoryState
        .flatMapLatest { category ->
            articleRepository.getTopHeadlinesPaging(country = "us", category = category.value)
        }
        .cachedIn(viewModelScope)

    init {
        _selectedCategoryState.value = Category.GENERAL
    }

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun loadHeadlines(category: Category = Category.GENERAL) {
        viewModelScope.launch {
//            _uiState.value = UiState.Loading
//            _selectedCategoryState.value = category
//            articleRepository.getTopHeadlines(country = "us", category = category.value)
//                .collect { result ->
//                    result.fold(
//                        onSuccess = { articles ->
//                            _uiState.value = UiState.Success(articles)
//                        },
//                        onFailure = { exception ->
//                            _uiState.value = UiState.Error(
//                                exception.message ?: "Unknown error occurred"
//                            )
//                        }
//                    )
//                }
        }
    }

    fun searchNews(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
//            _uiState.value = UiState.Loading
//
//            articleRepository.searchNews(query = query)
//                .collect { result ->
//                    result.fold(
//                        onSuccess = { articles ->
//                            _uiState.value = if (articles.isEmpty()) {
//                                UiState.Error("No Articles found for '$query'")
//                            } else {
//                                UiState.Success(articles)
//                            }
//                        },
//                        onFailure = { exception ->
//                            _uiState.value = UiState.Error(
//                                exception.message ?: "Search failed"
//                            )
//                        }
//                    )
//                }
        }
    }

    fun toggleFavorite(article: Article) {

        viewModelScope.launch {
            try {
                articleRepository.toggleFavorite(article)
            } catch (e: Exception) {
                //TODO
            }
        }
    }
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()

    data class Success(val articles: List<Article>) : UiState()
    data class Error(val message: String) : UiState()
}