package com.ians.observer.presentation.components

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ListUiStateTest {

    @Test
    fun `items present are always content`() {
        val loadStates = combinedLoadStates(mediatorRefresh = LoadState.Loading)

        assertEquals(ListUiState.Content, loadStates.toListUiState(itemCount = 3))
    }

    @Test
    fun `failed refresh does not blank out visible items`() {
        val loadStates = combinedLoadStates(mediatorRefresh = LoadState.Error(TestException()))

        assertEquals(ListUiState.Content, loadStates.toListUiState(itemCount = 3))
    }

    @Test
    fun `loading remote refresh with empty cache is loading`() {
        // Feed on a cold start: Room has nothing yet and the mediator is talking to the network.
        val loadStates = combinedLoadStates(
            sourceRefresh = LoadState.NotLoading(endOfPaginationReached = false),
            mediatorRefresh = LoadState.Loading
        )

        assertEquals(ListUiState.Loading, loadStates.toListUiState(itemCount = 0))
    }

    @Test
    fun `loading source refresh without mediator is loading`() {
        // Search: a plain PagingSource, so the source states are the network states.
        val loadStates = combinedLoadStates(sourceRefresh = LoadState.Loading)

        assertEquals(ListUiState.Loading, loadStates.toListUiState(itemCount = 0))
    }

    @Test
    fun `remote error with empty cache is error`() {
        val error = TestException()
        val loadStates = combinedLoadStates(mediatorRefresh = LoadState.Error(error))

        val state = loadStates.toListUiState(itemCount = 0)

        assertTrue(state is ListUiState.Error)
        assertSame(error, (state as ListUiState.Error).throwable)
    }

    @Test
    fun `settled refresh with no items is empty`() {
        val loadStates = combinedLoadStates(
            sourceRefresh = LoadState.NotLoading(endOfPaginationReached = true),
            mediatorRefresh = LoadState.NotLoading(endOfPaginationReached = true)
        )

        assertEquals(ListUiState.Empty, loadStates.toListUiState(itemCount = 0))
    }

    @Test
    fun `remote refreshing ignores the local source refresh`() {
        // Room re-reading its cache must not spin the pull-to-refresh indicator.
        val loadStates = combinedLoadStates(
            sourceRefresh = LoadState.Loading,
            mediatorRefresh = LoadState.NotLoading(endOfPaginationReached = false)
        )

        assertFalse(loadStates.isRemoteRefreshing)
    }

    @Test
    fun `remote refreshing follows the mediator`() {
        val loadStates = combinedLoadStates(mediatorRefresh = LoadState.Loading)

        assertTrue(loadStates.isRemoteRefreshing)
    }

    @Test
    fun `remote refreshing falls back to the source when there is no mediator`() {
        val loadStates = combinedLoadStates(sourceRefresh = LoadState.Loading)

        assertTrue(loadStates.isRemoteRefreshing)
    }

    @Test
    fun `refresh error is reported even when content is visible`() {
        val error = TestException()

        assertSame(error, combinedLoadStates(mediatorRefresh = LoadState.Error(error)).refreshError)
        assertNull(combinedLoadStates(mediatorRefresh = LoadState.Loading).refreshError)
    }

    @Test
    fun `append error is reported separately`() {
        val error = TestException()
        val loadStates = combinedLoadStates(append = LoadState.Error(error))

        assertSame(error, loadStates.appendError)
        assertNull(combinedLoadStates().appendError)
    }

    private fun combinedLoadStates(
        sourceRefresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        mediatorRefresh: LoadState? = null,
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    ): CombinedLoadStates {
        val prepend = LoadState.NotLoading(endOfPaginationReached = true)

        return CombinedLoadStates(
            refresh = mediatorRefresh ?: sourceRefresh,
            prepend = prepend,
            append = append,
            source = LoadStates(refresh = sourceRefresh, prepend = prepend, append = append),
            mediator = mediatorRefresh?.let {
                LoadStates(refresh = it, prepend = prepend, append = append)
            }
        )
    }
}

private class TestException : Exception("test failure")
