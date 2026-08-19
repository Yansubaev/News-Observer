package com.ians.observer.domain.model

enum class NewsCountry(
    val code: String
) {
    AR("ar"),
    AU("au"),
    AT("at"),
    BE("be"),
    BR("br"),
    BG("bg"),
    CA("ca"),
    CN("cn"),
    CO("co"),
    CU("cu"),
    CZ("cz"),
    EG("eg"),
    FR("fr"),
    DE("de"),
    GR("gr"),
    HK("hk"),
    HU("hu"),
    IN("in"),
    ID("id"),
    IE("ie"),
    IL("il"),
    IT("it"),
    JP("jp"),
    LV("lv"),
    LT("lt"),
    MY("my"),
    MX("mx"),
    MA("ma"),
    NL("nl"),
    NZ("nz"),
    NG("ng"),
    NO("no"),
    PH("ph"),
    PL("pl"),
    PT("pt"),
    RO("ro"),
    RU("ru"),
    SA("sa"),
    RS("rs"),
    SG("sg"),
    SK("sk"),
    SI("si"),
    ZA("za"),
    KR("kr"),
    SE("se"),
    CH("ch"),
    TW("tw"),
    TH("th"),
    TR("tr"),
    AE("ae"),
    UA("ua"),
    GB("gb"),
    US("us"),
    VE("ve");

    companion object {
        fun fromCode(code: String): NewsCountry =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: US
    }
}
