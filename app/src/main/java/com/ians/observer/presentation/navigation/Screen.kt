package com.ians.observer.presentation.navigation

import com.ians.observer.R

sealed class Screen(val route: String, val labelRes: Int, val iconRes: Int) {
    data object Home : Screen("home", R.string.nav_home, R.drawable.ic_home)
    data object Search : Screen("search", R.string.nav_search, R.drawable.ic_search)
    data object Favorites : Screen("favorites", R.string.nav_favorites, R.drawable.ic_favorites)
    data object Profile : Screen("profile", R.string.nav_profile, R.drawable.ic_profile)

    companion object {
        private const val ROUTE_HOME = "home"
        private const val ROUTE_SEARCH = "search"
        private const val ROUTE_FAV = "favorites"
        private const val ROUTE_PROFILE = "profile"
    }
}