package com.ians.observer.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.data.local.entity.categoryFromString
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.GENERAL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<List<Article>> = _selectedCategoryState.flatMapLatest { category ->
        articleRepository.getFavoriteArticlesForCategory(category.value ?: "")
    }

    private val _categories: MutableStateFlow<List<Category>> = MutableStateFlow(listOf())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        viewModelScope.launch {
            _categories.value = articleRepository.getFavoriteCategories().map { articles ->
                articles.map { article ->
                    categoryFromString(article) ?: Category.GENERAL
                }
            }.first()
        }

        _selectedCategoryState.value = Category.GENERAL
    }

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun toggleFavorite(article: Article) = viewModelScope.launch {
        articleRepository.toggleFavorite(article)
    }

}