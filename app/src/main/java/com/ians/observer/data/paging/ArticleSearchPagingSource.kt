package com.ians.observer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.remote.provider.NewsProvider
import com.ians.observer.data.remote.provider.model.PageToken
import com.ians.observer.data.remote.provider.model.SearchRequest
import com.ians.observer.data.remote.provider.model.toArticle
import com.ians.observer.domain.model.Article
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class ArticleSearchPagingSource(
    private val newsProvider: NewsProvider,
    private val database: NewsDatabase,
    private val query: String,
    private val language: NewsLanguage?,
    private val country: NewsCountry?,
    private val category: Category?

) : PagingSource<PageToken, Article>() {
    private val articleDao = database.articleDao()

    override fun getRefreshKey(state: PagingState<PageToken, Article>): PageToken? = null

    override suspend fun load(params: LoadParams<PageToken>): LoadResult<PageToken, Article> {
        return try {
            val providerPage = newsProvider.search(
                request = SearchRequest(
                    query = query,
                    country = country,
                    language = language,
                    category = category
                ),
                pageToken = params.key
            )

            val existingFavoriteUrls = articleDao.getFavoriteArticlesUrls().first()

            val articles = providerPage.articles.map {
                it.toArticle(
                    isFavorite = existingFavoriteUrls.contains(it.originalUrl)
                )
            }

            LoadResult.Page(
                data = articles,
                prevKey = null,
                nextKey = providerPage.nextPageToken
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}