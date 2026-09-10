package com.ians.observer.presentation.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.presentation.components.ArticleCard
import com.ians.observer.presentation.mapper.titleRes

@Composable
fun SavedScreen(
    nestedScrollConnection: NestedScrollConnection,
    viewModel: SavedViewModel = hiltViewModel(),
    onArticleClick: (Article) -> Unit
) {
    val articles by viewModel.articles.collectAsState(emptyList())
    val selectedCategory by viewModel.selectedCategoryState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (articles.isEmpty()) {
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
                onArticleClick = onArticleClick,
                onFavoriteClick = { article ->
                    viewModel.removeFromFavorites(article)
                }
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
                onClick = { viewModel.changeCategory(category) },
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

@Composable
fun SuccessContent(
    articles: List<Article>,
    nestedScrollConnection: NestedScrollConnection,
    onArticleClick: (Article) -> Unit,
    onFavoriteClick: (Article) -> Unit
) {
    if (articles.isEmpty()) {
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
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = CategoryBarHeight,
                end = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(
                count = articles.size
            ) { index ->
                val article = articles[index]
                ArticleCard(
                    article = article,
                    onArticleClick = { onArticleClick(article) },
                    onFavoriteClick = onFavoriteClick,
                )
            }
        }
    }
}

private val CategoryBarHeight = 52.dp
