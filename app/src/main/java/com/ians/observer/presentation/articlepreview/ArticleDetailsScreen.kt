package com.ians.observer.presentation.articlepreview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.ProviderId
import com.ians.observer.domain.model.Publisher

@Composable
fun ArticleDetailsScreen() {

}

@Composable
fun ArticleDetailsUiState(
    article: Article
) {

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ArticleDetailsUiPreview() {
    ArticleDetailsUiState(
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
}