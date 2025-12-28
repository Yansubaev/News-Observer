package com.ians.observer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchViewModel @Inject constructor (
    private val articleRepository: ArticleRepository
) : ViewModel() {

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

}