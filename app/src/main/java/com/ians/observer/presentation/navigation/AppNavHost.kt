package com.ians.observer.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ians.observer.domain.model.Article
import com.ians.observer.presentation.feed.FeedScreen
import com.ians.observer.presentation.saved.SavedScreen
import com.ians.observer.presentation.search.SearchScreen
import com.ians.observer.presentation.settings.AboutScreen
import com.ians.observer.presentation.settings.FeedProviderSettingsScreen
import com.ians.observer.presentation.settings.LanguageSettingsScreen
import com.ians.observer.presentation.settings.MainSettingsScreen
import com.ians.observer.presentation.settings.RegionSettingsScreen
import com.ians.observer.presentation.settings.SearchProviderSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    snackbarHostState: SnackbarHostState,
    paddingValues: PaddingValues,
    settingsRevealOrigin: Offset,
    onArticleSelected: (Article) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Feed.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = { navEnter() },
        exitTransition = { navExit() },
        popEnterTransition = { navPopEnter() },
        popExitTransition = { navPopExit() },
        predictivePopEnterTransition = { navPopEnter() },
        predictivePopExitTransition = { navPopExit() }
    ) {
        composable(Screen.Feed.route) {
            FeedScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                snackbarHostState = snackbarHostState,
                onArticleClick = onArticleSelected
            )
        }
        composable(Screen.Saved.route) {
            SavedScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                onArticleClick = onArticleSelected
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                snackbarHostState = snackbarHostState,
                onArticleClick = onArticleSelected
            )
        }

        composable(SettingsScreen.Main.route) {
            SettingsReveal(
                navController = navController,
                originInRoot = settingsRevealOrigin
            ) {
                MainSettingsScreen(navController)
            }
        }

        composable(SettingsScreen.FeedProviders.route) {
            FeedProviderSettingsScreen()
        }

        composable(SettingsScreen.SearchProviders.route) {
            SearchProviderSettingsScreen()
        }

        composable(SettingsScreen.Language.route) {
            LanguageSettingsScreen()
        }

        composable(SettingsScreen.Region.route) {
            RegionSettingsScreen()
        }

        composable(SettingsScreen.About.route) {
            AboutScreen()
        }
    }
}

@Composable
private fun AnimatedVisibilityScope.SettingsReveal(
    navController: NavHostController,
    originInRoot: Offset,
    content: @Composable () -> Unit,
) {
    var revealPlayed by rememberSaveable { mutableStateOf(false) }

    val currentEntry by navController.currentBackStackEntryAsState()
    val leavingSettings = currentEntry?.destination?.route !in SettingsScreen.routes

    val progress by transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) {
                tween(SettingsRevealDurationMillis, easing = LinearOutSlowInEasing)
            } else {
                tween(SettingsRevealDurationMillis, easing = FastOutLinearInEasing)
            }
        },
        label = "settings_reveal"
    ) { state ->
        when (state) {
            EnterExitState.PreEnter -> if (revealPlayed) 1f else 0f
            EnterExitState.Visible -> 1f
            EnterExitState.PostExit -> if (leavingSettings) 0f else 1f
        }
    }

    LaunchedEffect(transition.currentState) {
        if (transition.currentState == EnterExitState.Visible) revealPlayed = true
    }

    CircularReveal(
        progress = progress,
        originInRoot = originInRoot,
        content = content
    )
}
