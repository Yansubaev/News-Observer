package com.ians.observer.domain.usecase.search

import com.ians.observer.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSearchHistoryUseCase @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,

    ) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(query: String): Flow<List<String>> =
        searchHistoryRepository.getSearchHistory()
            .map { history ->
                if (query.isBlank()) {
                    history.takeLast(5)
                } else {
                    history.filter { it.contains(other = query, ignoreCase = true) }
                }
            }
}