package com.ians.observer.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/** How long the settings circle takes to cover, or uncover, the screen. */
const val SettingsRevealDurationMillis = 420

/** How long a push/pop inside the settings stack takes. */
const val StackDurationMillis = 300

private const val TabDurationMillis = 300

private const val FadeDurationMillis = 250

/**
 * Signed distance between two bottom bar tabs, or `0` when either side is not a tab.
 * Positive means the target sits to the right of the current screen.
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabShift(): Int {
    val from = Screen.bottomNavRoutes.indexOf(initialState.destination.route)
    val to = Screen.bottomNavRoutes.indexOf(targetState.destination.route)

    return if (from < 0 || to < 0) 0 else to - from
}

/** Slides the incoming tab in from the side it occupies in the bar; `null` between non-tabs. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition? {
    val shift = tabShift()
    if (shift == 0) return null

    val spec = tween<IntOffset>(TabDurationMillis, easing = FastOutSlowInEasing)

    return slideIntoContainer(
        towards = if (shift > 0) SlideDirection.Left else SlideDirection.Right,
        animationSpec = spec
    ) + fadeIn(tween(TabDurationMillis))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition? {
    val shift = tabShift()
    if (shift == 0) return null

    val spec = tween<IntOffset>(TabDurationMillis, easing = FastOutSlowInEasing)

    return slideOutOfContainer(
        towards = if (shift > 0) SlideDirection.Left else SlideDirection.Right,
        animationSpec = spec
    ) + fadeOut(tween(TabDurationMillis))
}

/** Pushing a screen onto the settings stack: it arrives from the right. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackEnter(): EnterTransition =
    slideIntoContainer(
        towards = SlideDirection.Left,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(StackDurationMillis))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackExit(): ExitTransition =
    slideOutOfContainer(
        towards = SlideDirection.Left,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(StackDurationMillis))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackPopEnter(): EnterTransition =
    slideIntoContainer(
        towards = SlideDirection.Right,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(StackDurationMillis))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackPopExit(): ExitTransition =
    slideOutOfContainer(
        towards = SlideDirection.Right,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(StackDurationMillis))

/**
 * "Animations" that change nothing but keep the screen on stage for [durationMillis].
 *
 * [EnterTransition.None] and [ExitTransition.None] finish instantly, which would tear the screen
 * underneath away before the settings circle has covered it. An imperceptible alpha step keeps it
 * composed for exactly as long as the reveal runs.
 */
private fun holdEnter(durationMillis: Int): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis), initialAlpha = HoldAlpha)

private fun holdExit(durationMillis: Int): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis), targetAlpha = HoldAlpha)

private fun defaultEnter(): EnterTransition = fadeIn(tween(FadeDurationMillis))

private fun defaultExit(): ExitTransition = fadeOut(tween(FadeDurationMillis))

/**
 * Every transition in the graph, resolved from the route pair.
 *
 * They live here rather than on the individual `composable` calls for two reasons: the settings
 * stack would otherwise repeat the same four lambdas five times, and `predictivePopEnterTransition`
 * / `predictivePopExitTransition` can only be set on `NavHost` — leaving them out is what made the
 * back button scale every screen down (the library default is `scaleOut(targetScale = 0.7f)`).
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.navEnter(): EnterTransition {
    val target = targetState.destination.route

    return when {
        // The settings root reveals itself with a circular clip, so it needs no transition here.
        target == SettingsScreen.Main.route -> EnterTransition.None
        target in SettingsScreen.routes -> stackEnter()
        else -> tabEnter() ?: defaultEnter()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navExit(): ExitTransition {
    val target = targetState.destination.route

    return when {
        // Hold the tab in place underneath while the settings circle grows over it.
        target == SettingsScreen.Main.route -> holdExit(SettingsRevealDurationMillis)
        target in SettingsScreen.routes -> stackExit()
        else -> tabExit() ?: defaultExit()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopEnter(): EnterTransition {
    val leaving = initialState.destination.route

    return when {
        // Coming back from the settings root: stay put and let the circle shrink away above.
        leaving == SettingsScreen.Main.route -> holdEnter(SettingsRevealDurationMillis)
        leaving in SettingsScreen.routes -> stackPopEnter()
        else -> tabEnter() ?: defaultEnter()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopExit(): ExitTransition {
    val leaving = initialState.destination.route

    return when {
        leaving == SettingsScreen.Main.route -> holdExit(SettingsRevealDurationMillis)
        leaving in SettingsScreen.routes -> stackPopExit()
        else -> tabExit() ?: defaultExit()
    }
}

private const val HoldAlpha = 0.99f
