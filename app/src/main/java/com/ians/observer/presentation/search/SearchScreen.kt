package com.ians.observer.presentation.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
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
import com.ians.observer.presentation.feed.SuccessContent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SearchScreen(
    nestedScrollConnection: NestedScrollConnection,
    viewModel: SearchViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit,
) {
    val articles = viewModel.searchResultArticles.collectAsLazyPagingItems()
    val liveQuery by viewModel.liveQuery.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    SearchScreenUI(
        query = liveQuery,
        onQueryChanged = { viewModel.setQueryText(it) },
        { viewModel.searchNews(it) },
        searchHistory = searchHistory,
        onSearchQueryClear = { viewModel.clearSearchQuery(it) },
        onToggleFavoriteArticle = { viewModel.setFavorite(it, !it.isFavorite) },
        nestedScrollConnection = nestedScrollConnection,
        articles = articles,
        onArticleClick = onArticleClick
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
        SearchHistoryHeaderHeight +
        (SearchHistoryItemHeight + SearchHistoryItemSpacing) * searchHistory.size

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
                Text(
                    stringResource(R.string.recents),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.height(SearchHistoryHeaderHeight)
                )

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

        SuccessContent(
            articles = articles,
            nestedScrollConnection = nestedScrollConnection,
            16.dp,
            onArticleClick = onArticleClick,
            onFavoriteClick = onToggleFavoriteArticle
        )
    }
}

@Composable
fun RecentsList(
    modifier: Modifier = Modifier,
    list: List<String>,
    onQueryClick: (String) -> Unit,
    onClearClick: (String) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Recents", style = MaterialTheme.typography.labelSmall)

        for (entry in list) {
            RecentQuery(
                text = entry,
                onClick = { onQueryClick(entry) },
                onClearClick = { onClearClick(entry) }
            )
        }
    }
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
        Icon(
            painterResource(R.drawable.watch),
            contentDescription = "watch icon",
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.size(12.dp))

        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onClearClick,
            modifier = Modifier.size(SearchHistoryItemHeight),
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_close_24),
                contentDescription = "close icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private val SearchHistoryHeaderHeight = 32.dp
private val SearchHistoryHorizontalPadding = 12.dp
private val SearchHistoryItemHeight = 48.dp
private val SearchHistoryItemSpacing = 16.dp
private val SearchHistoryVerticalPadding = 12.dp

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
            content = "Preview content one",
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
            content = "Preview content two",
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
            content = "Preview content three",
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
