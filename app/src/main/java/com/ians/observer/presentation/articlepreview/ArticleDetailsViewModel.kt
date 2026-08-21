package com.ians.observer.presentation.articlepreview

import androidx.lifecycle.ViewModel
import com.ians.observer.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticleDetailsViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {
}