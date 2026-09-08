package com.ians.observer.domain.usecase.feed

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.usecase.fake.FakeNewsProvidersRepository
import com.ians.observer.domain.usecase.fake.FakeSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAvailableCategoriesUseCaseTest {

    @Test
    fun `returns categories supported by selected feed provider`() = runTest {
        val settingsRepository = FakeSettingsRepository(feedProvider = ProviderId.NEWS_DATA)
        val providersRepository = FakeNewsProvidersRepository(
            categoriesByProvider = mutableMapOf(
                ProviderId.NEWS_DATA to setOf(Category.GENERAL, Category.SPORTS)
            )
        )
        val useCase = ObserveAvailableCategoriesUseCase(
            newsProvidersRepository = providersRepository,
            settingsRepository = settingsRepository,
        )

        val categories = useCase().take(1).toList().single()

        assertEquals(setOf(Category.GENERAL, Category.SPORTS), categories)
        assertEquals(
            listOf(ProviderId.NEWS_DATA),
            providersRepository.requestedCategoryProviderIds,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updates categories when selected provider changes`() = runTest {
        val settingsRepository = FakeSettingsRepository(feedProvider = ProviderId.NEWS_DATA)
        val providersRepository = FakeNewsProvidersRepository(
            categoriesByProvider = mutableMapOf(
                ProviderId.NEWS_DATA to setOf(Category.GENERAL),
                ProviderId.NEWS_API to setOf(Category.BUSINESS),
            )
        )
        val useCase = ObserveAvailableCategoriesUseCase(
            newsProvidersRepository = providersRepository,
            settingsRepository = settingsRepository,
        )
        val emissions = mutableListOf<Set<Category>>()
        val collectionJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().take(2).toList(emissions)
        }

        settingsRepository.feedProviderPreference.value = ProviderId.NEWS_API
        collectionJob.join()

        assertEquals(
            listOf(setOf(Category.GENERAL), setOf(Category.BUSINESS)),
            emissions,
        )
        assertEquals(
            listOf(ProviderId.NEWS_DATA, ProviderId.NEWS_API),
            providersRepository.requestedCategoryProviderIds,
        )
    }
}
