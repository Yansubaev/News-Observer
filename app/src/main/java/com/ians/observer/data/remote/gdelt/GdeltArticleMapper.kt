package com.ians.observer.data.remote.gdelt

import com.ians.observer.BuildConfig
import com.ians.observer.data.remote.gdelt.dto.GdeltArticleDto
import com.ians.observer.data.remote.provider.model.RemoteArticle
import com.ians.observer.domain.model.ProviderId
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private const val SeenDatePattern = "yyyyMMdd'T'HHmmss'Z'"

private val SpaceBeforePunctuation = Regex("""\s+([,.;:!?%])""")
private val SpaceAfterOpeningBracket = Regex("""([(\[{])\s+""")
private val SpaceBeforeClosingBracket = Regex("""\s+([)\]}])""")
private val SpaceBeforeApostrophe = Regex("""\s+(['’])\s*(?=\w)""")
private val RepeatedSpaces = Regex("""\s{2,}""")

fun List<GdeltArticleDto>.toRemoteArticles(): List<RemoteArticle> {
    val seenDateFormat = SimpleDateFormat(SeenDatePattern, Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }

    return map { it.toRemoteArticle(seenDateFormat) }
}

private fun GdeltArticleDto.toRemoteArticle(seenDateFormat: SimpleDateFormat) =
    RemoteArticle(
        id = this.url,
        providerId = ProviderId.GDELT,
        publisherId = this.domain,
        publisherName = this.domain,
        publisherWebsiteUrl = "https://${this.domain}",
        authors = null,
        title = this.title.normalizeTitle(),
        description = null,
        originalUrl = this.url,
        imageUrl = this.socialImage
            ?.takeIf { it.isNotBlank() && BuildConfig.IMAGES_ENABLED },
        publishedAt = runCatching { seenDateFormat.parse(this.seenDate) }
            .getOrNull()
            ?.time
            ?: System.currentTimeMillis(),
        category = null
    )

private fun String.normalizeTitle(): String = this
    .replace(SpaceBeforePunctuation, "$1")
    .replace(SpaceAfterOpeningBracket, "$1")
    .replace(SpaceBeforeClosingBracket, "$1")
    .replace(SpaceBeforeApostrophe, "$1")
    .replace(RepeatedSpaces, " ")
    .trim()
