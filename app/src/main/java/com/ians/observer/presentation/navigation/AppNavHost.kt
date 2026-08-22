package com.ians.observer.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ians.observer.domain.model.Article
import com.ians.observer.presentation.favorites.FavoritesScreen
import com.ians.observer.presentation.feed.FeedScreen
import com.ians.observer.presentation.search.SearchScreen
import com.ians.observer.presentation.settings.LanguageSettingsScreen
import com.ians.observer.presentation.settings.MainSettingsScreen
import com.ians.observer.presentation.settings.RegionSettingsScreen
import com.ians.observer.presentation.settings.FeedProviderSettingsScreen
import com.ians.observer.presentation.settings.SearchProviderSettingsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    paddingValues: PaddingValues,
    onArticleSelected: (Article) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Favorites.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }) {
        composable(Screen.Feed.route) {
            FeedScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                onArticleClick = onArticleSelected
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                onArticleClick = onArticleSelected
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                onArticleClick = onArticleSelected
            )
        }

        val enterTransition = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(250)
        )

        val popEnterTransition = scaleIn(
            initialScale = 0.95f, animationSpec = tween(250)
        )


        val exitTransition = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(250)
        )

        composable(
            SettingsScreen.Main.route,
            enterTransition = { enterTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { exitTransition }) {
            MainSettingsScreen(navController)
        }

        composable(
            SettingsScreen.FeedProviders.route,
            enterTransition = { enterTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { exitTransition }) {
            FeedProviderSettingsScreen(navController)
        }

        composable(
            SettingsScreen.SearchProviders.route,
            enterTransition = { enterTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { exitTransition }) {
            SearchProviderSettingsScreen(navController)
        }

        composable(
            SettingsScreen.Language.route,
            enterTransition = { enterTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { exitTransition }) {
            LanguageSettingsScreen()
        }

        composable(
            SettingsScreen.Region.route,
            enterTransition = { enterTransition },
            popEnterTransition = { popEnterTransition },
            popExitTransition = { exitTransition }) {
            RegionSettingsScreen()
        }
    }


}