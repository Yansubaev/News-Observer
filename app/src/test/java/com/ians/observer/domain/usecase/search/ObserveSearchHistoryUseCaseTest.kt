package com.ians.observer.domain.usecase.search

import com.ians.observer.domain.usecase.fake.FakeSearchHistoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveSearchHistoryUseCaseTest {

    @Test
    fun `returns last five history items for blank query`() = runTest {
        val repository = FakeSearchHistoryRepository(
            initialHistory = listOf("one", "two", "three", "four", "five", "six")
        )
        val useCase = ObserveSearchHistoryUseCase(repository)

        val history = useCase("   ").first()

        assertEquals(listOf("two", "three", "four", "five", "six"), history)
    }

    @Test
    fun `filters history by query ignoring case`() = runTest {
        val repository = FakeSearchHistoryRepository(
            initialHistory = listOf("Android", "Kotlin Flow", "Android Studio", "Compose")
        )
        val useCase = ObserveSearchHistoryUseCase(repository)

        val history = useCase("ANDROID").first()

        assertEquals(listOf("Android", "Android Studio"), history)
    }

    @Test
    fun `returns empty list when no history item matches query`() = runTest {
        val repository = FakeSearchHistoryRepository(initialHistory = listOf("Android", "Kotlin"))
        val useCase = ObserveSearchHistoryUseCase(repository)

        val history = useCase("Compose").first()

        assertEquals(emptyList<String>(), history)
    }
}
