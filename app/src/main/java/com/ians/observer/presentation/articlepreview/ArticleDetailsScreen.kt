package com.ians.observer.presentation.articlepreview

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import com.ians.observer.presentation.components.AnimatedFavoriteButton
import com.ians.observer.presentation.helper.OpenArticleResult
import com.ians.observer.presentation.helper.extractArticleHost
import com.ians.observer.presentation.helper.openArticleUrl
import com.ians.observer.presentation.helper.openUriInBrowser
import com.ians.observer.presentation.helper.parseArticleUri
import com.ians.observer.presentation.mapper.titleRes
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsSheet(
    articleId: String,
    viewModel: ArticleDetailsViewModel = hiltViewModel(),
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val closeFromButton: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissed()
            }
        }
    }

    val context = LocalContext.current
    val selectedArticle by viewModel.selectedArticleState.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismissed,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val uiState = selectedArticle) {
                ArticleDetailsUiState.Loading -> {
                    ArticleDetailsLoadingScreen(onClose = closeFromButton)
                }

                ArticleDetailsUiState.NotFound -> {
                    ArticleDetailsNotFoundScreen(onClose = closeFromButton)
                }

                is ArticleDetailsUiState.Error -> {
                    ArticleDetailsErrorScreen(
                        message = uiState.message,
                        onRetry = viewModel::retryLoading,
                        onClose = closeFromButton,
                    )
                }

                is ArticleDetailsUiState.Content -> {
                    val article = uiState.article
                    val shareContextTitle =
                        stringResource(R.string.article_details_share_chooser_title)
                    ArticleDetailsScreen(
                        article = article,
                        onClose = closeFromButton,
                        onSetFavorite = { viewModel.setFavorite(it) },
                        onShare = {
                            val text = "${article.title}\n${article.originalUrl}"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    /* target = */ shareIntent,
                                    /* title = */ shareContextTitle
                                )
                            )

                        },
                        onReadArticle = {
                            when (val openResult = openArticleUrl(article.originalUrl, context)) {
                                OpenArticleResult.Opened -> Unit
                                is OpenArticleResult.FailedToOpen -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(openResult.message)
                                    }
                                }
                            }
                        },
                        onOpenPublisher = {
                            parseArticleUri(article.publisher.websiteUrl)?.let { uri ->
                                when (val openResult = openUriInBrowser(uri, context)) {
                                    OpenArticleResult.Opened -> Unit
                                    is OpenArticleResult.FailedToOpen -> {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(openResult.message)
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsLoadingScreen(
    onClose: () -> Unit,
) {
    ArticleDetailsStatusScreen(
        onClose = onClose,
        title = stringResource(R.string.article_details_loading_title),
        message = stringResource(R.string.article_details_loading_message),
        illustration = {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsNotFoundScreen(
    onClose: () -> Unit,
) {
    ArticleDetailsStatusScreen(
        onClose = onClose,
        title = stringResource(R.string.article_details_not_found_title),
        message = stringResource(R.string.article_details_not_found_message),
        illustration = {
            ArticleDetailsStatusIcon(
                iconRes = R.drawable.ic_search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        actions = {
            Button(onClick = onClose) {
                Text(stringResource(R.string.article_details_close))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    ArticleDetailsStatusScreen(
        onClose = onClose,
        title = stringResource(R.string.article_details_error_title),
        message = message.ifBlank {
            stringResource(R.string.article_details_error_message)
        },
        illustration = {
            ArticleDetailsStatusIcon(
                iconRes = R.drawable.info,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        actions = {
            Button(onClick = onRetry) {
                Text(stringResource(R.string.article_details_retry))
            }

            TextButton(onClick = onClose) {
                Text(stringResource(R.string.article_details_close))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsStatusScreen(
    title: String,
    message: String,
    onClose: () -> Unit,
    illustration: @Composable () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.article_details_title)) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.cd_article_details_close),
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            illustration()

            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))
            actions()
        }
    }
}

@Composable
private fun ArticleDetailsStatusIcon(
    iconRes: Int,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = tint,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsScreen(
    article: Article,
    onClose: () -> Unit,
    onSetFavorite: (Boolean) -> Unit,
    onShare: () -> Unit,
    onReadArticle: () -> Unit,
    onOpenPublisher: () -> Unit,
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

    val publisherHost = remember(article.publisher.websiteUrl) {
        extractArticleHost(article.publisher.websiteUrl)
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
                AnimatedFavoriteButton(
                    isFavorite = article.isFavorite,
                    onClick = { onSetFavorite(!article.isFavorite) },
                    modifier = Modifier.size(48.dp)
                ) { tint, scale ->
                    Icon(
                        painter = if (article.isFavorite) {
                            painterResource(R.drawable.ic_favorites_filled)
                        } else {
                            painterResource(R.drawable.ic_favorites)
                        },
                        contentDescription = if (article.isFavorite) {
                            stringResource(R.string.cd_article_details_remove_from_favorites)
                        } else {
                            stringResource(R.string.cd_article_details_add_to_favorites)
                        },
                        tint = tint,
                        modifier = Modifier
                            .scale(scale)
                            .size(24.dp)
                    )
                }

                IconButton(onShare) {
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
                    contentDescription = null,
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
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    stringResource(R.string.article_details_original_publication),
                    color = MaterialTheme.colorScheme.secondary
                )

                publisherHost?.let { host ->
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                role = Role.Button,
                                onClick = onOpenPublisher,
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = host,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                }


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

            Spacer(Modifier.height(24.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .height(52.dp),
                onClick = onReadArticle,
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
                id = "labubu",
                websiteUrl = "https://example.com",
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
        onSetFavorite = {},
        onShare = {},
        onReadArticle = {},
        onOpenPublisher = {},
    )
}

@Preview(
    name = "Article details — Loading",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun ArticleDetailsLoadingScreenPreview() {
    ArticleDetailsLoadingScreen(onClose = {})
}

@Preview(
    name = "Article details — Not found",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun ArticleDetailsNotFoundScreenPreview() {
    ArticleDetailsNotFoundScreen(onClose = {})
}

@Preview(
    name = "Article details — Error",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun ArticleDetailsErrorScreenPreview() {
    ArticleDetailsErrorScreen(
        message = "The article could not be loaded. Check your connection and try again.",
        onRetry = {},
        onClose = {},
    )
}
