package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.repository.ArticleRepository
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DeleteStaleArticlesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()) {
        try {
            articleRepository.deleteCachedArticlesOlderThan(now - MAX_CACHE_AGE_MILLIS)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best effort: the next app start retries.
        }
    }

    companion object {
        // Google Play expects current-events apps to show content no older than 30 days.
        val MAX_CACHE_AGE_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
