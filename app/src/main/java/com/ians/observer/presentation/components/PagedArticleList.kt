package com.ians.observer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.ians.observer.R
import com.ians.observer.domain.model.Article

/**
 * Renders a paged list of articles together with every load state it can be in: skeletons for the
 * first page, an empty message, a full-screen error with retry, and an inline footer for
 * next-page loads and failures.
 *
 * Refresh failures are *not* handled here when content is already visible — the caller is expected
 * to surface those through a snackbar (see [com.ians.observer.presentation.components.refreshError]).
 */
@Composable
fun PagedArticleList(
    articles: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    onFavoriteClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 16.dp,
    nestedScrollConnection: NestedScrollConnection? = null,
    emptyMessage: String = stringResource(R.string.no_articles),
) {
    when (val state = articles.loadState.toListUiState(articles.itemCount)) {
        ListUiState.Loading -> ArticleListSkeleton(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalSpacing = verticalSpacing
        )

        ListUiState.Empty -> ArticleListMessage(
            modifier = modifier,
            contentPadding = contentPadding,
            title = emptyMessage
        )

        is ListUiState.Error -> ArticleListMessage(
            modifier = modifier,
            contentPadding = contentPadding,
            title = stringResource(R.string.list_error_title),
            message = state.throwable.message ?: stringResource(R.string.list_error_message),
            isError = true,
            onRetry = { articles.retry() }
        )

        ListUiState.Content -> ArticleListContent(
            articles = articles,
            onArticleClick = onArticleClick,
            onFavoriteClick = onFavoriteClick,
            modifier = modifier,
            contentPadding = contentPadding,
            verticalSpacing = verticalSpacing,
            nestedScrollConnection = nestedScrollConnection
        )
    }
}

@Composable
private fun ArticleListContent(
    articles: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    onFavoriteClick: (Article) -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
    verticalSpacing: Dp,
    nestedScrollConnection: NestedScrollConnection?,
) {
    val listModifier = modifier
        .fillMaxSize()
        .let { base ->
            if (nestedScrollConnection == null) base else base.nestedScroll(nestedScrollConnection)
        }

    LazyColumn(
        modifier = listModifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { article -> article.originalUrl }
        ) { index ->
            val article = articles[index]

            article?.let {
                ArticleCard(
                    article = it,
                    onArticleClick = { onArticleClick(it) },
                    onFavoriteClick = onFavoriteClick
                )
            }
        }

        if (articles.loadState.append is LoadState.Loading || articles.loadState.appendError != null) {
            item(key = AppendFooterKey) {
                AppendFooter(
                    isLoading = articles.loadState.append is LoadState.Loading,
                    onRetry = { articles.retry() }
                )
            }
        }
    }
}

@Composable
private fun AppendFooter(
    isLoading: Boolean,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.list_append_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

/**
 * Empty / error placeholder.
 *
 * It is built on a [LazyColumn] with a single full-height item rather than a plain [Column]: a
 * pull-to-refresh gesture only reaches the parent when its child dispatches nested scroll, and a
 * non-scrollable container never does. The lazy list keeps dispatching even with nothing to scroll.
 */
@Composable
fun ArticleListMessage(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    message: String? = null,
    isError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillParentMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                onRetry?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = it) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
    }
}

private const val AppendFooterKey = "append_footer"
