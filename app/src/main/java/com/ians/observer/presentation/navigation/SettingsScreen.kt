package com.ians.observer.presentation.navigation

import com.ians.observer.R
import javax.xml.transform.Source

sealed class SettingsScreen(val route: String, val name: Int) {
    data object Main : SettingsScreen(ROUTE_MAIN, R.string.settings)
    data object Sources : SettingsScreen(ROUTE_SOURCES, R.string.settings_sources)
    data object Language : SettingsScreen(ROUTE_LANG, R.string.settings_language)
    data object Region : SettingsScreen(ROUTE_REGION, R.string.settings_region)

    companion object {
        const val ROUTE_MAIN = "settings/main"
        const val ROUTE_SOURCES = "settings/sources"
        const val ROUTE_LANG = "settings/lang"
        const val ROUTE_REGION = "settings/region"

        val routes: List<String> = listOf(
            ROUTE_MAIN,
            ROUTE_SOURCES,
            ROUTE_LANG,
            ROUTE_REGION
        )
    }
}