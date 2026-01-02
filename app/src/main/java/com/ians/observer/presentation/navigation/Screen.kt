package com.ians.observer.presentation.navigation

import com.ians.observer.R

enum class Screen(val route: String, val label: Int, val icon: Int) {
    HOME("home", R.string.nav_home, R.drawable.ic_home),
    SEARCH("search", R.string.nav_search, R.drawable.ic_search),
    FAVORITES("favorites", R.string.nav_favorites, R.drawable.ic_favorites),
    PROFILE("profile", R.string.nav_profile, R.drawable.ic_profile),
}