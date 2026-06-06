package com.ians.observer.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ians.observer.presentation.favorites.FavoritesScreen
import com.ians.observer.presentation.home.HomeScreen
import com.ians.observer.presentation.home.HomeScreenState
import com.ians.observer.presentation.search.SearchScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(scrollBehavior.nestedScrollConnection)
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(scrollBehavior.nestedScrollConnection)
        }
        composable(Screen.Search.route) {
            SearchScreen(scrollBehavior.nestedScrollConnection)
        }
    }


}