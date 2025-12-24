package com.ians.observer.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.presentation.test.HomeViewModel
import com.ians.observer.presentation.test.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val selectedCategory by viewModel.selectedCategoryState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    scrollBehavior = scrollBehavior
                )

                Row(
                    modifier = Modifier
                        .defaultMinSize()
                        .horizontalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries.forEach { category ->
                        FilterChip(
                            selected = category == selectedCategory,
                            onClick = { viewModel.changeCategory(category) },
                            label = { Text(stringResource(category.stringRes)) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                    onFavoriteClick = { article ->
                        viewModel.toggleFavorite(article)
                    }
                )
            }
//            when (articles.loadState.) {
//                is UiState.Initial -> {
//                    InitialContent()
//                }
//
//                is UiState.Loading -> {
//                    LoadingContent()
//                }
//
//                is UiState.Success -> {
//                    SuccessContent(
//                        articles = (uiState as UiState.Success).articles,
//                        onFavoriteClick = { article ->
//                            viewModel.toggleFavorite(article)
//                        })
//                }
//
//                is UiState.Error -> {
//                    ErrorContent(message = (uiState as UiState.Error).message)
//                }
//            }
        }
    }
}

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
    onFavoriteClick: (Article) -> Unit
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(
                count = articles.itemCount,
                key = articles.itemKey { article -> article.url }
            ) { index ->
                val article = articles[index]

                article?.let {
                    ArticleCard(article = it, onFavoriteClick)
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

@Composable
fun ArticleCard(
    article: Article,
    onFavoriteClick: (Article) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(0.dp)
        ) {
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Source
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                AnimatedFavoriteButton(
                    isFavorite = article.isFavorite,
                    onClick = { onFavoriteClick(article) },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            article.description?.let { description ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Publish date
            Text(
                text = "Published: ${article.publishedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val tint by animateColorAsState(
        targetValue = if (isFavorite) {
            Color(0xFFFF6B35)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "tint"
    )

    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onClick() },
        modifier = modifier
    ) {
        Icon(
            painter = if (isFavorite) {
                painterResource(R.drawable.favorite_enabled)
            } else {
                painterResource(R.drawable.favorite_disabled)
            },
            contentDescription = if (isFavorite) {
                "Remove from favorites"
            } else {
                "Add to favorites"
            },
            tint = tint,
            modifier = Modifier
                .scale(scale)
                .size(16.dp)
        )
    }
}