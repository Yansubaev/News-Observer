package com.ians.observer.presentation.search

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.room.util.query
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.presentation.home.SuccessContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.exp

@Composable
fun SearchScreen(
    nestedScrollConnection: NestedScrollConnection,
    viewModel: SearchViewModel = hiltViewModel()
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
        nestedScrollConnection = nestedScrollConnection,
        articles = articles
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
    nestedScrollConnection: NestedScrollConnection?,
    articles: LazyPagingItems<Article>
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val horizontalPadding by animateDpAsState(
        targetValue = if (expanded) 12.dp else 24.dp,
        label = "search_bar_padding"
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DockedSearchBar(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .background(Color.Green)
                .fillMaxWidth(),

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
            expanded = expanded,
            onExpandedChange = { expanded = it },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                dividerColor = Color.Transparent,
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .background(Color.Red)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Recents", style = MaterialTheme.typography.labelSmall, modifier = Modifier.height(32.dp))

                for (entry in searchHistory) {
                    RecentQuery(entry) { onSearchQueryClear(entry) }
                }
            }
        }

        SuccessContent(
            articles = articles,
            nestedScrollConnection = nestedScrollConnection,
            16.dp
        ) { }
    }
}

@Composable
fun RecentsList(
    modifier: Modifier = Modifier,
    list: List<String>,
    onClearClick: (String) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Recents", style = MaterialTheme.typography.labelSmall)

        for (entry in list) {
            RecentQuery(entry) { onClearClick(entry) }
        }
    }
}

@Composable
fun RecentQuery(
    text: String,
    modifier: Modifier = Modifier,
    onClearClick: () -> Unit
) {
    Row(
        modifier = modifier,
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
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onClearClick,
            modifier = Modifier
                .weight(0.1f)
                .aspectRatio(1f),
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    val fakeArticles = listOf(
        Article(
            id = "1",
            page = 1,
            sourceId = "source-1",
            sourceName = "Observer Daily",
            author = "Ada Lovelace",
            title = "Breaking News One",
            description = "First fake article for preview",
            url = "https://example.com/article-1",
            imageUrl = null,
            publishedAt = 1710000000000,
            content = "Preview content one",
            isFavorite = false,
            category = Category.GENERAL
        ),
        Article(
            id = "2",
            page = 1,
            sourceId = "source-2",
            sourceName = "Observer Weekly",
            author = "Grace Hopper",
            title = "Breaking News Two",
            description = "Second fake article for preview",
            url = "https://example.com/article-2",
            imageUrl = null,
            publishedAt = 1710003600000,
            content = "Preview content two",
            isFavorite = true,
            category = Category.SPORTS
        ),
        Article(
            id = "3",
            page = 1,
            sourceId = "source-3",
            sourceName = "Observer Tech",
            author = "Alan Turing",
            title = "Breaking News Three",
            description = "Third fake article for preview",
            url = "https://example.com/article-3",
            imageUrl = null,
            publishedAt = 1710007200000,
            content = "Preview content three",
            isFavorite = false,
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
        null,
        articles
    )
}
