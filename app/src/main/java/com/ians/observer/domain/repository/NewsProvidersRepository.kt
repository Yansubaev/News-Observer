package com.ians.observer.domain.repository

import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId

interface NewsProvidersRepository {
    fun getSupportedCategoriesForProviderId(id: ProviderId): Set<Category>
    fun getTopHeadlinesCapableProviderIds(): Set<ProviderId>
    fun getSearchCapableProviderIds(): Set<ProviderId>
    fun getAvailableProviderIds(): Set<ProviderId>
}