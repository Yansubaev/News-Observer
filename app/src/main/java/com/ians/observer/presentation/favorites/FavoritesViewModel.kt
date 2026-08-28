package com.ians.observer.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class FavoritesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _selectedCategoryState = MutableStateFlow(Category.ALL)
    val selectedCategoryState: StateFlow<Category> = _selectedCategoryState.asStateFlow()

    val categories: StateFlow<List<Category>> =
        articleRepository.getFavoriteCategories().map { strings ->
            listOf(Category.ALL) + strings
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: Flow<List<Article>> = _selectedCategoryState.flatMapLatest { category ->
        if (category == Category.ALL) {
            articleRepository.getFavoriteArticles()
        } else {
            articleRepository.getFavoriteArticlesForCategory(category)
        }
    }

    fun changeCategory(category: Category) {
        _selectedCategoryState.value = category
    }

    fun removeFromFavorites(article: Article) = viewModelScope.launch {
        articleRepository.removeFromFavorites(article.id)
    }

}