package com.ians.observer.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.presentation.components.PagedArticleList
import com.ians.observer.presentation.components.RefreshErrorSnackbar
import com.ians.observer.presentation.components.isRemoteRefreshing
import com.ians.observer.presentation.components.refreshError
import com.ians.observer.presentation.mapper.titleRes
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FeedScreen(
    nestedScrollConnection: NestedScrollConnection,
    snackbarHostState: SnackbarHostState,
    viewModel: FeedViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit,
) {
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val selectedCategory by viewModel.selectedCategoryState.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle(
        initialValue = emptySet()
    )

    FeedScreenState(
        articles = articles,
        categories = availableCategories,
        selectedCategory = selectedCategory,
        snackbarHostState = snackbarHostState,
        onToggleFavoriteArticle = { viewModel.toggleFavorite(it) },
        onChangeCategory = { viewModel.changeCategory(it) },
        onArticleClick = onArticleClick,
        nestedScrollConnection = nestedScrollConnection
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreenState(
    articles: LazyPagingItems<Article>,
    categories: Set<Category>,
    selectedCategory: Category,
    onToggleFavoriteArticle: (Article) -> Unit,
    onChangeCategory: (Category) -> Unit,
    onArticleClick: (Article) -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    val loadState = articles.loadState

    // The pull indicator only speaks for refreshes of an already visible list; when there is
    // nothing on screen the skeletons carry the message instead.
    val isRefreshing = loadState.isRemoteRefreshing && articles.itemCount > 0

    RefreshErrorSnackbar(
        error = loadState.refreshError,
        hasContent = articles.itemCount > 0,
        snackbarHostState = snackbarHostState,
        onRetry = { articles.retry() }
    )

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        state = pullToRefreshState,
        onRefresh = { articles.refresh() },
        indicator = {
            // Offset so the spinner lands below the floating category chips instead of behind them.
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = CategoryBarHeight),
                isRefreshing = isRefreshing,
                state = pullToRefreshState
            )
        }
    ) {
        PagedArticleList(
            articles = articles,
            contentPadding = PaddingValues(
                start = 16.dp,
                top = CategoryBarHeight,
                end = 16.dp,
                bottom = 24.dp
            ),
            nestedScrollConnection = nestedScrollConnection,
            onArticleClick = onArticleClick,
            onFavoriteClick = onToggleFavoriteArticle
        )
    }

    Row(
        modifier = Modifier
            .defaultMinSize()
            .horizontalScroll(rememberScrollState())
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onChangeCategory(category) },
                label = { Text(stringResource(category.titleRes)) },
                colors = FilterChipDefaults.filterChipColors().copy(
                    containerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            )
        }
    }
}

private val CategoryBarHeight = 52.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeedScreenPreview() {
    val fakeArticles = listOf(
        Article(
            id = "1",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu",
                websiteUrl = "https://google.com",
            ),
            authors = setOf("Ada Lovelace"),
            title = "Breaking News One",
            description = "First fake article for preview",
            originalUrl = "https://example.com/article-1",
            imageUrl = null,
            publishedAt = 1710000000000,
            isFavorite = false,
            providerId = ProviderId.NEWS_API,
            category = Category.GENERAL
        ),
        Article(
            id = "2",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu",
                websiteUrl = "https://google.com",
            ),
            authors = setOf("Grace Hopper"),
            title = "Breaking News Two",
            description = "Second fake article for preview",
            originalUrl = "https://example.com/article-2",
            imageUrl = null,
            publishedAt = 1710003600000,
            isFavorite = true,
            providerId = ProviderId.NEWS_API,
            category = Category.SPORTS
        ),
        Article(
            id = "3",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu",
                websiteUrl = "https://google.com",
            ),
            authors = setOf("Alan Turing"),
            title = "Breaking News Three",
            description = "Third fake article for preview",
            originalUrl = "https://example.com/article-3",
            imageUrl = null,
            publishedAt = 1710007200000,
            isFavorite = false,
            providerId = ProviderId.NEWS_API,
            category = Category.TECHNOLOGY
        )
    )

    val articlesFlow = remember {
        MutableStateFlow(PagingData.from(fakeArticles))
    }
    val articles = articlesFlow.collectAsLazyPagingItems()

    FeedScreenState(
        articles = articles,
        categories = Category.entries.toSet(),
        selectedCategory = Category.GENERAL,
        onToggleFavoriteArticle = {},
        onChangeCategory = {},
        onArticleClick = {}
    )
}
