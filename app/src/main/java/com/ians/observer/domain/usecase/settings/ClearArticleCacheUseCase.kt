package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class ClearArticleCacheUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(): Boolean =
        try {
            articleRepository.clearCachedArticles()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
}
