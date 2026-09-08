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
    fun `calls success callback after cache is cleared`() = runTest {
        val repository = FakeArticleRepository()
        val useCase = ClearArticleCacheUseCase(repository)
        var successCalled = false
        var failureCalled = false

        useCase(
            onSuccess = { successCalled = true },
            onFailed = { failureCalled = true },
        )

        assertEquals(1, repository.clearCacheCallCount)
        assertTrue(successCalled)
        assertFalse(failureCalled)
    }

    @Test
    fun `passes repository exception to failure callback`() = runTest {
        val expectedException = IllegalStateException("Cannot clear cache")
        val repository = FakeArticleRepository().apply {
            clearCacheException = expectedException
        }
        val useCase = ClearArticleCacheUseCase(repository)
        var successCalled = false
        var actualException: Exception? = null

        useCase(
            onSuccess = { successCalled = true },
            onFailed = { actualException = it },
        )

        assertEquals(1, repository.clearCacheCallCount)
        assertFalse(successCalled)
        assertSame(expectedException, actualException)
    }

    @Test
    fun `rethrows cancellation without invoking callbacks`() = runTest {
        val expectedException = CancellationException("Test cancellation")
        val repository = FakeArticleRepository().apply {
            clearCacheException = expectedException
        }
        val useCase = ClearArticleCacheUseCase(repository)
        var successCalled = false
        var failureCalled = false
        var actualException: CancellationException? = null

        try {
            useCase(
                onSuccess = { successCalled = true },
                onFailed = { failureCalled = true },
            )
        } catch (exception: CancellationException) {
            actualException = exception
        }

        assertSame(expectedException, actualException)
        assertFalse(successCalled)
        assertFalse(failureCalled)
    }
}
