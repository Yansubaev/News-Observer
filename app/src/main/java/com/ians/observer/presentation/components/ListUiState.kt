package com.ians.observer.presentation.components

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

/**
 * UI-facing projection of [CombinedLoadStates] plus the current item count.
 *
 * Paging exposes two independent sets of load states and mixing them up is the usual source of
 * flickering spinners:
 *  - [CombinedLoadStates.source] is the [androidx.paging.PagingSource]. For the feed it reads Room,
 *    so it settles almost immediately — long before the network answers.
 *  - [CombinedLoadStates.mediator] is the [androidx.paging.RemoteMediator] and is `null` when the
 *    pager has none (search). This is the state that actually reflects a network call.
 *
 * Everything below therefore prefers the mediator when it exists and falls back to the source.
 */
sealed interface ListUiState {

    /** First page is on its way and there is nothing to show yet. */
    data object Loading : ListUiState

    /** Loading finished successfully, but the result is empty. */
    data object Empty : ListUiState

    /** Refresh failed and there is no cached content to fall back on. */
    data class Error(val throwable: Throwable) : ListUiState

    /** There are items to render. */
    data object Content : ListUiState
}

/**
 * Maps load states to a [ListUiState]. Items always win: once something is on screen a failed or
 * ongoing refresh must not blank it out — that is reported through [refreshError] and
 * [isRemoteRefreshing] instead.
 */
fun CombinedLoadStates.toListUiState(itemCount: Int): ListUiState {
    if (itemCount > 0) return ListUiState.Content

    val remoteRefresh = mediator?.refresh
    val sourceRefresh = source.refresh

    return when {
        remoteRefresh is LoadState.Loading || sourceRefresh is LoadState.Loading -> ListUiState.Loading
        remoteRefresh is LoadState.Error -> ListUiState.Error(remoteRefresh.error)
        sourceRefresh is LoadState.Error -> ListUiState.Error(sourceRefresh.error)
        else -> ListUiState.Empty
    }
}

/** `true` while a refresh that hits the network is in flight. Drives the pull-to-refresh spinner. */
val CombinedLoadStates.isRemoteRefreshing: Boolean
    get() = (mediator?.refresh ?: source.refresh) is LoadState.Loading

/** Refresh failure, regardless of whether stale content is still being displayed. */
val CombinedLoadStates.refreshError: Throwable?
    get() = (mediator?.refresh as? LoadState.Error)?.error
        ?: (source.refresh as? LoadState.Error)?.error

/** Failure of the next-page load. Reported inline at the bottom of the list. */
val CombinedLoadStates.appendError: Throwable?
    get() = (append as? LoadState.Error)?.error
        ?: (mediator?.append as? LoadState.Error)?.error
