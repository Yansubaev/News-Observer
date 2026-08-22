package com.ians.observer.presentation.articlepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.presentation.mapper.titleRes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsSheet(
    article: Article,
    onDismissed: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val closeFromButton = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissed()
            }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismissed
    ) {
        ArticleDetailsScreen(
            article = article,
            onClose = {
                closeFromButton()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsScreen(
    article: Article,
    onClose: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Details") },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        painterResource(R.drawable.baseline_close_24),
                        contentDescription = "close button icon",
                    )
                }
            },
            actions = {
                IconButton({}) {
                    Icon(
                        painter = painterResource(R.drawable.ic_favorites),
                        contentDescription = "favorites icon"
                    )
                }
                IconButton({}) {
                    Icon(
                        painter = painterResource(R.drawable.sources),
                        contentDescription = "share icon"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(
                    bottom = 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
        ) {
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = article.publisher.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            // Publish date
            val publishedAt = Date(article.publishedAt)
            val formatter = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.getDefault())
            val formattedDate = formatter.format(publishedAt)
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            article.authors?.let { authors ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Authors: " + authors.joinToString(", "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            article.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            Text("Original text: ${article.publisher.name}")
            Text("Data porivder: ${stringResource(article.providerId.titleRes)}")

            Spacer(Modifier.weight(1f))

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                ,
                onClick = {},
            ) {
                Text("Read in the publishers site")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArticleDetailsUiPreview() {
    ArticleDetailsScreen(
        Article(
            id = "3",
            publisher = Publisher(
                name = "Labubu News",
                id = "labubu"
            ),
            authors = setOf("Alan Turing", "Ada Lovelace"),
            title = "Breaking News Three",
            description = "Third fake article for preview. " +
                    "Long enough text to fit two line in a Text component. " +
                    "Suppose to simulate description text for article",
            originalUrl = "https://example.com/article-3",
            imageUrl = null,
            publishedAt = 1710007200000,
            content = "Preview content three",
            isFavorite = false,
            providerId = ProviderId.NEWS_API,
            category = Category.TECHNOLOGY
        ),
        onClose = {},
    )
}