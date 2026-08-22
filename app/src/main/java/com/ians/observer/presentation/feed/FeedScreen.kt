package com.ians.observer.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.presentation.mapper.titleRes
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FeedScreen(
    nestedScrollConnection: NestedScrollConnection,
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
    nestedScrollConnection: NestedScrollConnection? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (articles.itemCount == 0) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.Center),
                text = stringResource(R.string.no_articles)
            )
        } else {
            SuccessContent(
                articles = articles,
                nestedScrollConnection = nestedScrollConnection,
                52.dp,
                onFavoriteClick = { article ->
                    onToggleFavoriteArticle(article)
                },
                onArticleClick = onArticleClick
            )
        }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InitialContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ready to test API!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Click a button above to load news",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SuccessContent(
    articles: LazyPagingItems<Article>,
    nestedScrollConnection: NestedScrollConnection?,
    topPadding: Dp,
    onFavoriteClick: (Article) -> Unit,
    onArticleClick: (Article) -> Unit
) {
    if (articles.itemCount == 0) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            text = stringResource(R.string.no_articles),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        val mod = if (nestedScrollConnection == null)
            Modifier.fillMaxSize() else
            Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)

        LazyColumn(
            modifier = mod,
            contentPadding = PaddingValues(24.dp, topPadding, 24.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
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

            item {
                when (val state = articles.loadState.append) {
                    is LoadState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is LoadState.Error -> {
                        ErrorContent("ERROR")
                    }

                    is LoadState.NotLoading -> {
                        if (articles.itemCount == 0) {
                            InitialContent()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ErrorContentPreview() {
    ErrorContent("Mock error text")
}

@Composable
fun ErrorContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FeedScreenPreview() {
    val fakeArticles = listOf(
        Article(
            id = "1",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu"
            ),
            authors = setOf("Ada Lovelace"),
            title = "Breaking News One",
            description = "First fake article for preview",
            originalUrl = "https://example.com/article-1",
            imageUrl = null,
            publishedAt = 1710000000000,
            content = "Preview content one",
            isFavorite = false,
            providerId = ProviderId.NEWS_API,
            category = Category.GENERAL
        ),
        Article(
            id = "2",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu"
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
            category = Category.SPORTS
        ),
        Article(
            id = "3",
            publisher = Publisher(
                name = "Labubu",
                id = "labubu"
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

    FeedScreenState(
        articles = articles,
        categories = Category.entries.toSet(),
        selectedCategory = Category.GENERAL,
        onToggleFavoriteArticle = {},
        onChangeCategory = {},
        onArticleClick = {}
    )
}
