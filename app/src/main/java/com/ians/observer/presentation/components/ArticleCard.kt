package com.ians.observer.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ians.observer.R
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher
import java.text.SimpleDateFormat
import java.util.Date


@Preview
@Composable
private fun ArticleCardPreview() {
    ArticleCard(
        Article(
            id = "",
            publisher = Publisher(
                name = "BBC",
                id = "bbc",
                websiteUrl = "https://google.com"
            ),
            authors = setOf("Denis Ians"),
            title = "Preview article",
            description = "This is a preview article",
            originalUrl = "https://ichef.bbci.co.uk/images/ic/1920x1080/p0nhlk0l.jpg.webp",
            publishedAt = 0,
            imageUrl = "",
            isFavorite = true,
            category = Category.TECHNOLOGY,
            providerId = ProviderId.NEWS_API
        ),
        onArticleClick = {}
    ) { }
}

@Composable
fun ArticleCard(
    article: Article,
    onArticleClick: () -> Unit,
    onFavoriteClick: (Article) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onArticleClick, role = Role.Button),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            article.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .height(180.dp),
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
                    text = article.publisher.name,
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
            val locale = LocalLocale.current.platformLocale
            val formatter = remember(locale) { SimpleDateFormat(PublishedAtPattern, locale) }
            val formattedDate = remember(formatter, article.publishedAt) {
                formatter.format(Date(article.publishedAt))
            }

            Text(
                text = formattedDate,
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
    modifier: Modifier,
    icon: @Composable ((Color, Float) -> Unit)? = null
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
            MaterialTheme.colorScheme.onSurfaceVariant
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
        if (icon == null) {
            Icon(
                painter = if (isFavorite) {
                    painterResource(R.drawable.ic_favorites_filled)
                } else {
                    painterResource(R.drawable.ic_favorites)
                },
                contentDescription = if (isFavorite) {
                    stringResource(R.string.cd_article_details_unsave_article)
                } else {
                    stringResource(R.string.cd_article_details_save_article)
                }, tint = tint,
                modifier = Modifier
                    .scale(scale)
                    .size(16.dp)
            )
        } else {
            icon.invoke(tint, scale)
        }
    }
}

private const val PublishedAtPattern = "EEEE, d MMMM HH:mm"
