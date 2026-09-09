package com.ians.observer.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.ians.observer.R

/**
 * A refresh that fails while stale content is on screen must not replace that content, so it is
 * reported through a snackbar with a retry action.
 */
@Composable
fun RefreshErrorSnackbar(
    error: Throwable?,
    hasContent: Boolean,
    snackbarHostState: SnackbarHostState?,
    onRetry: () -> Unit,
) {
    if (snackbarHostState == null) return

    val message = stringResource(R.string.list_refresh_failed)
    val retryLabel = stringResource(R.string.action_retry)

    LaunchedEffect(error, hasContent) {
        if (error == null || !hasContent) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = retryLabel
        )

        if (result == SnackbarResult.ActionPerformed) onRetry()
    }
}
