package com.ians.observer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ians.observer.R

/**
 * Placeholder list shown while the first page is loading.
 *
 * It intentionally mirrors the geometry of [ArticleCard] (padding, image height, spacing) so the
 * layout does not jump once real articles arrive. Scrolling is disabled: there is nothing to scroll
 * to yet, and it keeps the pull-to-refresh gesture from fighting an inert list.
 */
@Composable
fun ArticleListSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalSpacing: Dp = 16.dp,
    itemCount: Int = DefaultSkeletonItemCount,
) {
    val loadingDescription = stringResource(R.string.cd_loading_articles)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = loadingDescription },
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        userScrollEnabled = false
    ) {
        items(itemCount) { index ->
            ArticleCardSkeleton(showImage = index % 3 != 2)
        }
    }
}

@Composable
fun ArticleCardSkeleton(
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showImage) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .shimmer(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Source row: publisher name + favorite button.
            Row(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonLine(widthFraction = 0.3f, height = 12.dp)

                Spacer(
                    modifier = Modifier
                        .size(28.dp)
                        .shimmer(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title: two lines of labelLarge.
            SkeletonLine(widthFraction = 1f, height = 14.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonLine(widthFraction = 0.65f, height = 14.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Description: three lines of bodySmall.
            SkeletonLine(widthFraction = 1f, height = 10.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonLine(widthFraction = 1f, height = 10.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonLine(widthFraction = 0.45f, height = 10.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Publish date.
            SkeletonLine(widthFraction = 0.4f, height = 10.dp)
        }
    }
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .shimmer()
    )
}

internal const val DefaultSkeletonItemCount = 4

@Preview(showBackground = true)
@Composable
private fun ArticleCardSkeletonPreview() {
    ArticleCardSkeleton()
}
