package com.ians.observer.domain.usecase.settings

import com.ians.observer.domain.usecase.fake.FakeArticleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.util.concurrent.TimeUnit

class DeleteStaleArticlesUseCaseTest {

    @Test
    fun `deletes articles older than 30 days`() = runTest {
        val repository = FakeArticleRepository()
        val now = TimeUnit.DAYS.toMillis(100)

        DeleteStaleArticlesUseCase(repository)(now)

        assertEquals(listOf(TimeUnit.DAYS.toMillis(70)), repository.deleteOlderThanThresholds)
    }

    @Test
    fun `swallows repository failure`() = runTest {
        val repository = FakeArticleRepository().apply {
            deleteOlderThanException = IllegalStateException("Database closed")
        }

        DeleteStaleArticlesUseCase(repository)()

        assertEquals(1, repository.deleteOlderThanThresholds.size)
    }

    @Test
    fun `rethrows cancellation`() = runTest {
        val expectedException = CancellationException("Test cancellation")
        val repository = FakeArticleRepository().apply {
            deleteOlderThanException = expectedException
        }
        var actualException: CancellationException? = null

        try {
            DeleteStaleArticlesUseCase(repository)()
        } catch (exception: CancellationException) {
            actualException = exception
        }

        assertSame(expectedException, actualException)
    }
}
