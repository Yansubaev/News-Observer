package com.ians.observer.presentation.navigation

import com.ians.observer.R

sealed class SettingsScreen(val route: String, val name: Int) {
    data object Main : SettingsScreen(ROUTE_MAIN, R.string.settings)
    data object FeedProviders : SettingsScreen(ROUTE_FEED_PROVIDERS, R.string.settings_feed_providers)
    data object SearchProviders : SettingsScreen(ROUTE_SEARCH_PROVIDERS, R.string.settings_search_providers)
    data object Language : SettingsScreen(ROUTE_LANG, R.string.settings_language)
    data object Region : SettingsScreen(ROUTE_REGION, R.string.settings_region)

    companion object {
        const val ROUTE_MAIN = "settings/main"
        const val ROUTE_FEED_PROVIDERS = "settings/feed_providers"
        const val ROUTE_SEARCH_PROVIDERS = "settings/search_providers"
        const val ROUTE_LANG = "settings/lang"
        const val ROUTE_REGION = "settings/region"

        val routes: List<String> = listOf(
            ROUTE_MAIN,
            ROUTE_FEED_PROVIDERS,
            ROUTE_SEARCH_PROVIDERS,
            ROUTE_LANG,
            ROUTE_REGION
        )
    }
}