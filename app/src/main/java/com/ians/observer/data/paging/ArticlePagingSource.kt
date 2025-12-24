package com.ians.observer.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ians.observer.data.local.dao.ArticleDao
import com.ians.observer.data.local.entity.toArticle
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.data.remote.dto.toArticle
import com.ians.observer.domain.model.Article
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ArticlePagingSource(
    private val newsApi: NewsApi,
    private val articleDao: ArticleDao,
    private val country: String,
    private val category: String? = null
) : PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val currentPage = params.key ?: 1
            Log.d("ArticlePagingSource", "Loading page: $currentPage")

            val response = newsApi.getHeadlines(
                country = country,
                category = category,
                page = currentPage,
                pageSize = ITEMS_PER_PAGE
            )

            val favorites = articleDao.getFavoriteArticles()
                .map { entities -> entities.map { it.url } }
                .first()

            val articles = response.articles.map { dto ->
                dto.toArticle(favorites.contains(dto.url))
            }

            LoadResult.Page(
                data = articles,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (articles.isEmpty()) {
                    null
                } else if (currentPage * ITEMS_PER_PAGE >= response.totalResults) {
                    null
                } else {
                    currentPage + 1
                }
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val ITEMS_PER_PAGE = 20
    }
}