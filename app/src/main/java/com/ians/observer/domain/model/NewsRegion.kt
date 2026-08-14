package com.ians.observer.domain.model

import androidx.annotation.StringRes
import com.ians.observer.R

enum class NewsRegion(
    val code: String,
    @get:StringRes val titleRes: Int,
) {
    AR("ar", R.string.news_region_ar),
    AU("au", R.string.news_region_au),
    AT("at", R.string.news_region_at),
    BE("be", R.string.news_region_be),
    BR("br", R.string.news_region_br),
    BG("bg", R.string.news_region_bg),
    CA("ca", R.string.news_region_ca),
    CN("cn", R.string.news_region_cn),
    CO("co", R.string.news_region_co),
    CU("cu", R.string.news_region_cu),
    CZ("cz", R.string.news_region_cz),
    EG("eg", R.string.news_region_eg),
    FR("fr", R.string.news_region_fr),
    DE("de", R.string.news_region_de),
    GR("gr", R.string.news_region_gr),
    HK("hk", R.string.news_region_hk),
    HU("hu", R.string.news_region_hu),
    IN("in", R.string.news_region_in),
    ID("id", R.string.news_region_id),
    IE("ie", R.string.news_region_ie),
    IL("il", R.string.news_region_il),
    IT("it", R.string.news_region_it),
    JP("jp", R.string.news_region_jp),
    LV("lv", R.string.news_region_lv),
    LT("lt", R.string.news_region_lt),
    MY("my", R.string.news_region_my),
    MX("mx", R.string.news_region_mx),
    MA("ma", R.string.news_region_ma),
    NL("nl", R.string.news_region_nl),
    NZ("nz", R.string.news_region_nz),
    NG("ng", R.string.news_region_ng),
    NO("no", R.string.news_region_no),
    PH("ph", R.string.news_region_ph),
    PL("pl", R.string.news_region_pl),
    PT("pt", R.string.news_region_pt),
    RO("ro", R.string.news_region_ro),
    RU("ru", R.string.news_region_ru),
    SA("sa", R.string.news_region_sa),
    RS("rs", R.string.news_region_rs),
    SG("sg", R.string.news_region_sg),
    SK("sk", R.string.news_region_sk),
    SI("si", R.string.news_region_si),
    ZA("za", R.string.news_region_za),
    KR("kr", R.string.news_region_kr),
    SE("se", R.string.news_region_se),
    CH("ch", R.string.news_region_ch),
    TW("tw", R.string.news_region_tw),
    TH("th", R.string.news_region_th),
    TR("tr", R.string.news_region_tr),
    AE("ae", R.string.news_region_ae),
    UA("ua", R.string.news_region_ua),
    GB("gb", R.string.news_region_gb),
    US("us", R.string.news_region_us),
    VE("ve", R.string.news_region_ve);

    companion object {
        fun fromCode(code: String): NewsRegion =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown NewsRegion code: $code")
    }
}