package com.ians.observer.domain.model

enum class NewsLanguage(
    val code: String
) {
    AR("ar"),
    DE("de"),
    EN("en"),
    ES("es"),
    FR("fr"),
    HE("he"),
    IT("it"),
    NL("nl"),
    NO("no"),
    PT("pt"),
    RU("ru"),
    SV("sv"),
    UD("ud"),
    ZH("zh");

    companion object {
        fun fromCode(code: String): NewsLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: EN
    }
}
