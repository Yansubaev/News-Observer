package com.ians.observer.presentation.articlepreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import java.text.DateFormat
import java.util.Date

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

    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = remember(article.publishedAt, locale) {
        DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.SHORT,
            locale
        ).format(Date(article.publishedAt))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.article_details_title)) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.cd_article_details_close),
                    )
                }
            },
            actions = {
                IconButton({}) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_favorites),
                        contentDescription = stringResource(R.string.cd_article_details_add_to_favorites)
                    )
                }
                IconButton({}) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.sources),
                        contentDescription = stringResource(R.string.cd_article_details_share)
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
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(16.dp))
            }

            //region Metadata
            Text(
                text = article.publisher.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            //endregion Metadata

            Spacer(Modifier.height(16.dp))

            //region Content
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            val authors = article.authors
                .orEmpty()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .sorted()

            if (authors.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = pluralStringResource(
                        id = R.plurals.article_details_authors,
                        count = authors.size,
                        authors.joinToString(", ")
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            article.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
            }
            //endregion Content

            //region Original
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    stringResource(R.string.article_details_original_publication),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = article.publisher.name,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(Modifier.height(4.dp))

                val providerText = when (article.providerId) {
                    else -> stringResource(
                        R.string.article_details_data_provider,
                        stringResource(article.providerId.titleRes)
                    )
                }
                Text(providerText, color = MaterialTheme.colorScheme.secondary)

            }
            //endregion Original

//            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .height(52.dp),
                onClick = {},
            ) {
                Text(
                    stringResource(
                        R.string.article_details_read_on_publisher_site,
                        article.publisher.name
                    )
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_open_in_new),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.article_details_opens_on_publisher_site),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
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