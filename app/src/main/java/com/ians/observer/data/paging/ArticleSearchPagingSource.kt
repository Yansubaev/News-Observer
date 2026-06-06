package com.ians.observer.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ians.observer.data.local.dao.NewsDatabase
import com.ians.observer.data.remote.api.NewsApi
import com.ians.observer.data.remote.dto.toArticle
import com.ians.observer.domain.model.Article
import com.ians.observer.ui.theme.errorDark

class ArticleSearchPagingSource(
    private val api: NewsApi,
    private val database: NewsDatabase,
    private val query: String,
    private val language: String?

) : PagingSource<Int, Article>() {
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1

        return try {
            val response = api.searchNews(
                query = query,
                page = page,
                pageSize = params.loadSize,
                language = language
            )

            if (response.status != "ok") {
                return LoadResult.Error(Exception("API error: ${response.status}"))
            }

            val articles = response.articles.map {
                it.toArticle(
                    page = page,
                    isFavorite = database.articleDao().isFavorite(it.url)
                )
            }

            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}