package com.ians.observer.presentation.navigation

import com.ians.observer.R

sealed class Screen(val route: String, val labelRes: Int, val iconRes: Int) {
    data object Feed : Screen(ROUTE_FEED, R.string.nav_feed, R.drawable.ic_home)
    data object Search : Screen(ROUTE_SEARCH, R.string.nav_search, R.drawable.ic_search)
    data object Saved : Screen(ROUTE_SAVED, R.string.nav_saved, R.drawable.ic_favorites)

    companion object {
        private const val ROUTE_FEED = "feed"
        private const val ROUTE_SEARCH = "search"
        private const val ROUTE_SAVED = "saved"

        val bottomNavItems: List<Screen> by lazy { listOf(Feed, Search, Saved) }

        val bottomNavRoutes: List<String> by lazy { bottomNavItems.map { it.route } }
    }
}

