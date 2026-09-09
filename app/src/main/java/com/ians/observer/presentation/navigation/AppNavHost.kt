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
        startDestination = Screen.Saved.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = { navEnter() },
        exitTransition = { navExit() },
        popEnterTransition = { navPopEnter() },
        popExitTransition = { navPopExit() },
        // The back button and the back gesture both arrive here. Without these two the library
        // default scales every screen down to 0.7 on the way out.
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
            FeedProviderSettingsScreen(navController)
        }

        composable(SettingsScreen.SearchProviders.route) {
            SearchProviderSettingsScreen(navController)
        }

        composable(SettingsScreen.Language.route) {
            LanguageSettingsScreen()
        }

        composable(SettingsScreen.Region.route) {
            RegionSettingsScreen()
        }
    }
}

/**
 * Grows the settings root out of the settings button, and shrinks it back into the button on the
 * way out.
 *
 * The progress is animated off [AnimatedVisibilityScope.transition] rather than a standalone
 * `Animatable` so that a predictive back gesture drags the circle with the finger and putting the
 * finger back cancels it — that transition is seekable, a hand-rolled animation would not be.
 *
 * Two things have to be told apart, and neither is visible from the transition alone:
 *  - a first appearance versus coming back from a settings sub-screen, kept in [rememberSaveable]
 *    so it survives leaving the composition while the entry stays on the back stack;
 *  - leaving settings altogether versus pushing a sub-screen on top, which is read off the
 *    controller: only the former should collapse the circle, the latter slides away as a whole.
 */
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
                // Decelerating: the circle starts up in the app bar, above the NavHost, so a slow
                // start would be spent growing outside the visible content.
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

    // Recorded only once the screen has settled, so the flag never changes the value the entering
    // animation started from.
    LaunchedEffect(transition.currentState) {
        if (transition.currentState == EnterExitState.Visible) revealPlayed = true
    }

    CircularReveal(
        progress = progress,
        originInRoot = originInRoot,
        content = content
    )
}
