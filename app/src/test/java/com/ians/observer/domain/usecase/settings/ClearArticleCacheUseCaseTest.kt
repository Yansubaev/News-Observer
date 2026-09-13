package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ClearArticleCacheUseCaseTest {

    @Test
    fun `returns true after cache is cleared`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = ClearArticleCacheUseCase(repository)

        val result = useCase()

        assertEquals(1, repository.clearCacheCallCount)
        assertTrue(result)
    }

    @Test
    fun `returns false when repository fails`() = runTest {
        val repository = FakeArticleRepository().apply {
            clearCacheException = IllegalStateException("Cannot clear cache")
        }
        val useCase = ClearArticleCacheUseCase(repository)

        val result = useCase()

        assertEquals(1, repository.clearCacheCallCount)
        assertFalse(result)
    }

    @Test
    fun `rethrows cancellation`() = runTest {
        val expectedException = CancellationException("Test cancellation")
        val repository = FakeArticleRepository().apply {
            clearCacheException = expectedException
        }
        val useCase = ClearArticleCacheUseCase(repository)
        var actualException: CancellationException? = null

        try {
            useCase()
        } catch (exception: CancellationException) {
            actualException = exception
        }

        assertSame(expectedException, actualException)
    }
}
