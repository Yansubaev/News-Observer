package com.ians.observer.presentation.articlepreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {
    private val selectedArticleId = MutableStateFlow<String?>(null)
    private val reloadTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedArticleState: StateFlow<ArticleDetailsUiState> = combine(
        selectedArticleId.filterNotNull(),
        reloadTrigger,
    ) { articleId, _ ->
        articleId
    }
        .flatMapLatest { articleId ->
            articleRepository
                .observeArticleById(id = articleId)
                .map { article ->
                    article?.let(ArticleDetailsUiState::Content)
                        ?: ArticleDetailsUiState.NotFound
                }
                .onStart { emit(ArticleDetailsUiState.Loading) }
                .catch { error ->
                    emit(ArticleDetailsUiState.Error(error.localizedMessage.orEmpty()))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArticleDetailsUiState.Loading
        )

    fun loadArticle(id: String) {
        selectedArticleId.value = id
    }

    fun retryLoading() {
        reloadTrigger.update { it + 1 }
    }

    fun setFavorite(shouldBeFavorite: Boolean) = viewModelScope.launch {
        val article = (selectedArticleState.value as? ArticleDetailsUiState.Content)?.article
            ?: return@launch

        if (shouldBeFavorite) {
            articleRepository.addToFavorites(article, article.category)
        } else {
            articleRepository.removeFromFavorites(article.id)
        }
    }
}
