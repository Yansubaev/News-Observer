package com.ians.observer.presentation.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.ians.observer.R
import java.util.Locale

fun parseArticleUri(rawUrl: String?): Uri? {
    val value = rawUrl?.trim().orEmpty()
    if (value.isEmpty()) return null

    val uri = value.toUri()
    val scheme = uri.scheme?.lowercase(Locale.ROOT)

    return if (scheme in setOf("http", "https") && !uri.host.isNullOrBlank()) uri else null
}

fun extractArticleHost(rawUrl: String?): String? {
    return parseArticleUri(rawUrl)
        ?.host
        ?.trim()
        ?.trimEnd('.')
        ?.lowercase(Locale.ROOT)
        ?.removePrefix("www.")
        ?.takeIf(String::isNotBlank)
}

sealed interface OpenArticleResult {
    data object Opened : OpenArticleResult
    data class FailedToOpen(val message: String) : OpenArticleResult
}

fun openArticleUrl(
    url: String,
    context: Context,
): OpenArticleResult {
    val uri = parseArticleUri(url)
        ?: return OpenArticleResult.FailedToOpen(
            context.getString(R.string.article_details_invalid_url)
        )

    return try {
        val customTabPackage = CustomTabsClient.getPackageName(context, null)
        if (customTabPackage != null) {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build()
                .apply {
                    intent.setPackage(customTabPackage)
                }

            customTabsIntent.launchUrl(context, uri)
            OpenArticleResult.Opened
        } else {
            openUriInBrowser(
                uri = uri,
                context = context,
            )
        }
    } catch (e: ActivityNotFoundException) {
        openUriInBrowser(
            uri = uri,
            context = context,
        )
    }
}

fun openEmailComposer(
    email: String,
    subject: String,
    context: Context,
    body: String? = null,
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        body?.let { putExtra(Intent.EXTRA_TEXT, it) }
    }

    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

fun openUriInBrowser(
    uri: Uri,
    context: Context,
): OpenArticleResult {
    return try {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        context.startActivity(browserIntent)
        OpenArticleResult.Opened
    } catch (_: ActivityNotFoundException) {
        OpenArticleResult.FailedToOpen(
            context.getString(R.string.article_details_browser_not_found)
        )
    }
}