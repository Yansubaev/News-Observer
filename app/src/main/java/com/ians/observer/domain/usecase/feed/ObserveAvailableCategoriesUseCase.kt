package com.ians.observer.domain.usecase.feed

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.repository.NewsProvidersRepository
import com.ians.observer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAvailableCategoriesUseCase @Inject constructor(
    private val newsProvidersRepository: NewsProvidersRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Set<Category>> =
        settingsRepository.observeFeedProviderPreference()
            .map { providerId ->
                newsProvidersRepository.getSupportedCategoriesForProviderId(providerId)
            }
            .distinctUntilChanged()

}