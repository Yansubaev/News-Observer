package com.ians.observer.presentation.articlepreview

import com.ians.observer.domain.model.Article

sealed interface ArticleDetailsUiState {
    data object Loading : ArticleDetailsUiState
    data class Content(val article: Article) : ArticleDetailsUiState
    data object NotFound : ArticleDetailsUiState
    data class Error(val message: String) : ArticleDetailsUiState
}