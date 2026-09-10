package com.ians.observer.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

const val SettingsRevealDurationMillis = 420

const val StackDurationMillis = 300

private const val TabDurationMillis = 300

private const val FadeDurationMillis = 250

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabShift(): Int {
    val from = Screen.bottomNavRoutes.indexOf(initialState.destination.route)
    val to = Screen.bottomNavRoutes.indexOf(targetState.destination.route)

    return if (from < 0 || to < 0) 0 else to - from
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition? {
    val shift = tabShift()
    if (shift == 0) return null

    return slideIntoContainer(
        towards = if (shift > 0) SlideDirection.Left else SlideDirection.Right,
        animationSpec = tween(TabDurationMillis, easing = FastOutSlowInEasing)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition? {
    val shift = tabShift()
    if (shift == 0) return null

    return slideOutOfContainer(
        towards = if (shift > 0) SlideDirection.Left else SlideDirection.Right,
        animationSpec = tween(TabDurationMillis, easing = FastOutSlowInEasing)
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackEnter(): EnterTransition =
    slideIntoContainer(
        towards = SlideDirection.Left,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackExit(): ExitTransition =
    slideOutOfContainer(
        towards = SlideDirection.Left,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackPopEnter(): EnterTransition =
    slideIntoContainer(
        towards = SlideDirection.Right,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.stackPopExit(): ExitTransition =
    slideOutOfContainer(
        towards = SlideDirection.Right,
        animationSpec = tween(StackDurationMillis, easing = FastOutSlowInEasing)
    )

private fun holdEnter(durationMillis: Int): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis), initialAlpha = HoldAlpha)

private fun holdExit(durationMillis: Int): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis), targetAlpha = HoldAlpha)

private fun defaultEnter(): EnterTransition = fadeIn(tween(FadeDurationMillis))

private fun defaultExit(): ExitTransition = fadeOut(tween(FadeDurationMillis))

fun AnimatedContentTransitionScope<NavBackStackEntry>.navEnter(): EnterTransition {
    val target = targetState.destination.route

    return when (target) {
        SettingsScreen.Main.route -> EnterTransition.None
        in SettingsScreen.routes -> stackEnter()
        else -> tabEnter() ?: defaultEnter()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navExit(): ExitTransition {
    val target = targetState.destination.route

    return when (target) {
        SettingsScreen.Main.route -> holdExit(SettingsRevealDurationMillis)
        in SettingsScreen.routes -> stackExit()
        else -> tabExit() ?: defaultExit()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopEnter(): EnterTransition {
    val leaving = initialState.destination.route

    return when (leaving) {
        SettingsScreen.Main.route -> holdEnter(SettingsRevealDurationMillis)
        in SettingsScreen.routes -> stackPopEnter()
        else -> tabEnter() ?: defaultEnter()
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.navPopExit(): ExitTransition {
    val leaving = initialState.destination.route

    return when (leaving) {
        SettingsScreen.Main.route -> holdExit(SettingsRevealDurationMillis)
        in SettingsScreen.routes -> stackPopExit()
        else -> tabExit() ?: defaultExit()
    }
}

private const val HoldAlpha = 1f
