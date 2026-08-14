package com.ians.observer.domain.model

import androidx.annotation.StringRes
import com.ians.observer.R

enum class NewsLanguage(
    val code: String,
    @get:StringRes val titleRes: Int,
) {
    AR("ar", R.string.news_language_ar),
    DE("de", R.string.news_language_de),
    EN("en", R.string.news_language_en),
    ES("es", R.string.news_language_es),
    FR("fr", R.string.news_language_fr),
    HE("he", R.string.news_language_he),
    IT("it", R.string.news_language_it),
    NL("nl", R.string.news_language_nl),
    NO("no", R.string.news_language_no),
    PT("pt", R.string.news_language_pt),
    RU("ru", R.string.news_language_ru),
    SV("sv", R.string.news_language_sv),
    UD("ud", R.string.news_language_ud),
    ZH("zh", R.string.news_language_zh);

    companion object {
        fun fromCode(code: String): NewsLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown NewsLanguage code: $code")
    }
}