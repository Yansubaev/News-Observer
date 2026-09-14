package com.ians.observer.presentation.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.presentation.components.ArticleListMessage
import com.ians.observer.presentation.components.PagedArticleList
import com.ians.observer.presentation.components.RefreshErrorSnackbar
import com.ians.observer.presentation.components.isRemoteRefreshing
import com.ians.observer.presentation.components.refreshError
import com.ians.observer.presentation.mapper.titleRes
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SearchScreen(
    nestedScrollConnection: NestedScrollConnection,
    snackbarHostState: SnackbarHostState,
    viewModel: SearchViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit,
) {
    val articles = viewModel.searchResultArticles.collectAsLazyPagingItems()
    val liveQuery by viewModel.liveQuery.collectAsStateWithLifecycle()
    val submittedQuery by viewModel.queryState.collectAsStateWithLifecycle()
    val hasQuery by viewModel.hasQuery.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val enabledSearchProviderIds by viewModel.enabledSearchProviderIds.collectAsStateWithLifecycle()
    val selectedSearchProvider by viewModel.selectedSearchProvider.collectAsStateWithLifecycle()

    SearchScreenUI(
        query = liveQuery,
        onQueryChanged = { viewModel.setQueryText(it) },
        { viewModel.searchNews(it) },
        searchHistory = searchHistory,
        onSearchQueryClear = { viewModel.clearSearchQuery(it) },
        onToggleFavoriteArticle = { viewModel.setFavorite(it, !it.isFavorite) },
        nestedScrollConnection = nestedScrollConnection,
        articles = articles,
        onArticleClick = onArticleClick,
        submittedQuery = submittedQuery,
        hasQuery = hasQuery,
        snackbarHostState = snackbarHostState,
        enabledSearchProviderIds = enabledSearchProviderIds,
        selectedSearchProvider = selectedSearchProvider,
        onSearchProviderSelected = viewModel::selectSearchProvider
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenUI(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchRequested: (String) -> Unit,
    searchHistory: List<String>,
    onSearchQueryClear: (String) -> Unit,
    onToggleFavoriteArticle: (Article) -> Unit,
    nestedScrollConnection: NestedScrollConnection?,
    articles: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    submittedQuery: String = "",
    hasQuery: Boolean = false,
    snackbarHostState: SnackbarHostState? = null,
    enabledSearchProviderIds: Set<ProviderId> = emptySet(),
    selectedSearchProvider: ProviderId? = null,
    onSearchProviderSelected: (ProviderId) -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) 12.dp else 24.dp,
        label = "search_bar_padding"
    )
    // DockedSearchBar otherwise enforces 240.dp for its expanded content.
    val searchBarMaxHeight = SearchBarDefaults.InputFieldHeight +
        DividerDefaults.Thickness +
        SearchHistoryVerticalPadding * 2 +
        SearchHistoryItemHeight * searchHistory.size +
        SearchHistoryItemSpacing * (searchHistory.size - 1).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DockedSearchBar(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .heightIn(max = searchBarMaxHeight),

            inputField = {
                SearchBarDefaults.InputField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    query = query,
                    onQueryChange = onQueryChanged,
                    onSearch = onSearchRequested,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.search_news),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search),
                            "Search bar"
                        )
                    },
                    trailingIcon = {
                        if (!query.isBlank()) {
                            IconButton({
                                onQueryChanged("")
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_close_24),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                )
            },
            expanded = expanded && searchHistory.isNotEmpty(),
            onExpandedChange = { expanded = it },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                dividerColor = Color.Transparent,
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = SearchHistoryHorizontalPadding,
                        vertical = SearchHistoryVerticalPadding
                    )
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(SearchHistoryItemSpacing)
            ) {
                for (entry in searchHistory) {
                    RecentQuery(
                        text = entry,
                        onClick = {
                            onQueryChanged(entry)
                            onSearchRequested(entry)
                            expanded = false
                        },
                        onClearClick = { onSearchQueryClear(entry) }
                    )
                }
            }
        }

        selectedSearchProvider
            ?.takeIf { it in enabledSearchProviderIds && enabledSearchProviderIds.size > 1 }
            ?.let { selectedProviderId ->
            SearchProviderSelector(
                providerIds = ProviderId.entries.filter { it in enabledSearchProviderIds },
                selectedProviderId = selectedProviderId,
                onProviderSelected = onSearchProviderSelected
            )
            }

        SearchResults(
            articles = articles,
            hasQuery = hasQuery,
            submittedQuery = submittedQuery,
            snackbarHostState = snackbarHostState,
            nestedScrollConnection = nestedScrollConnection,
            onArticleClick = onArticleClick,
            onToggleFavoriteArticle = onToggleFavoriteArticle
        )
    }
}

@Composable
private fun SearchProviderSelector(
    providerIds: List<ProviderId>,
    selectedProviderId: ProviderId,
    onProviderSelected: (ProviderId) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Text(
                    stringResource(
                        R.string.search_provider,
                        stringResource(selectedProviderId.titleRes)
                    )
                )
            },
            trailingIcon = { Text("⌄") }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            providerIds.forEach { providerId ->
                DropdownMenuItem(
                    text = { Text(stringResource(providerId.titleRes)) },
                    onClick = {
                        onProviderSelected(providerId)
                        expanded = false
                    },
                    trailingIcon = {
                        if (providerId == selectedProviderId) {
                            RadioButton(selected = true, onClick = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SearchResults(
    articles: LazyPagingItems<Article>,
    hasQuery: Boolean,
    submittedQuery: String,
    snackbarHostState: SnackbarHostState?,
    nestedScrollConnection: NestedScrollConnection?,
    onArticleClick: (Article) -> Unit,
    onToggleFavoriteArticle: (Article) -> Unit,
) {
    val loadState = articles.loadState
    val hasResults = articles.itemCount > 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SearchProgressBarHeight)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasQuery && loadState.isRemoteRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }

    RefreshErrorSnackbar(
        error = loadState.refreshError,
        hasContent = hasResults,
        snackbarHostState = snackbarHostState,
        onRetry = { articles.retry() }
    )

    if (!hasQuery) {
        ArticleListMessage(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.search_prompt_title),
            message = stringResource(R.string.search_prompt_message)
        )
        return
    }

    PagedArticleList(
        articles = articles,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(16.dp),
        nestedScrollConnection = nestedScrollConnection,
        emptyMessage = stringResource(R.string.search_no_results, submittedQuery),
        onArticleClick = onArticleClick,
        onFavoriteClick = onToggleFavoriteArticle
    )
}

@Composable
fun RecentQuery(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchHistoryItemHeight)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Same 48.dp slot as the clear button, so both icons sit symmetrically.
        Box(
            modifier = Modifier.size(SearchHistoryIconSlotSize),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(R.drawable.watch),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onClearClick,
            modifier = Modifier.size(SearchHistoryIconSlotSize),
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_close_24),
                contentDescription = "close icon",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

// With the 48.dp icon slots this puts icons and text on the search field's keylines.
private val SearchHistoryHorizontalPadding = 4.dp
private val SearchHistoryIconSlotSize = 48.dp
private val SearchHistoryItemHeight = 48.dp
private val SearchHistoryItemSpacing = 16.dp
private val SearchHistoryVerticalPadding = 12.dp
private val SearchProgressBarHeight = 12.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
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
            category = Category.GENERAL,
            providerId = ProviderId.NEWS_API
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
            category = Category.SPORTS,
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

    SearchScreenUI(
        "",
        {},
        {},
        searchHistory = listOf("First", "second"),
        {},
        onToggleFavoriteArticle = {},
        null,
        articles,
        onArticleClick = {}
    )
}
