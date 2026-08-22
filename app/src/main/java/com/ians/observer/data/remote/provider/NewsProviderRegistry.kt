package com.ians.observer.data.remote.provider

import com.ians.observer.data.remote.provider.model.ProviderCapabilities
import com.ians.observer.domain.model.ProviderId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsProviderRegistry @Inject constructor(
    private val providers: Map<ProviderId, @JvmSuppressWildcards NewsProvider>
) {
    val availableIds: Set<ProviderId>
        get() = providers.keys

    fun require(id: ProviderId): NewsProvider =
        requireNotNull(providers[id]) {
            "Provider $id is not available in this build"
        }

    fun requireAll(ids: Collection<ProviderId>): List<NewsProvider> =
        ids.distinct().map(::require)

    fun idsSupporting(capability: ProviderCapabilities): List<ProviderId> =
        providers.filter { it.value.capabilities.contains(capability) }.map { it.key }
}