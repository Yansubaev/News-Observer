package com.ians.observer.presentation.mapper

import androidx.annotation.StringRes
import com.ians.observer.R
import com.ians.observer.domain.model.Category
import com.ians.observer.domain.model.NewsCountry
import com.ians.observer.domain.model.NewsLanguage
import com.ians.observer.domain.model.ProviderId

@get:StringRes
val Category.titleRes: Int
    get() = when (this) {
        Category.GENERAL -> R.string.cat_general
        Category.BREAKING -> R.string.cat_breaking
        Category.BUSINESS -> R.string.cat_business
        Category.CRIME -> R.string.cat_crime
        Category.DOMESTIC -> R.string.cat_domestic
        Category.EDUCATION -> R.string.cat_education
        Category.ENTERTAINMENT -> R.string.cat_entertainment
        Category.ENVIRONMENT -> R.string.cat_environment
        Category.FOOD -> R.string.cat_food
        Category.HEALTH -> R.string.cat_health
        Category.LIFESTYLE -> R.string.cat_lifestyle
        Category.OTHER -> R.string.cat_other
        Category.POLITICS -> R.string.cat_politics
        Category.SCIENCE -> R.string.cat_science
        Category.SPORTS -> R.string.cat_sports
        Category.TECHNOLOGY -> R.string.cat_technology
        Category.TOURISM -> R.string.cat_tourism
        Category.WORLD -> R.string.cat_world
        Category.ALL -> R.string.cat_all
    }

@get:StringRes
val NewsCountry.titleRes: Int
    get() = when (this) {
        NewsCountry.AR -> R.string.news_region_ar
        NewsCountry.AU -> R.string.news_region_au
        NewsCountry.AT -> R.string.news_region_at
        NewsCountry.BE -> R.string.news_region_be
        NewsCountry.BR -> R.string.news_region_br
        NewsCountry.BG -> R.string.news_region_bg
        NewsCountry.CA -> R.string.news_region_ca
        NewsCountry.CN -> R.string.news_region_cn
        NewsCountry.CO -> R.string.news_region_co
        NewsCountry.CU -> R.string.news_region_cu
        NewsCountry.CZ -> R.string.news_region_cz
        NewsCountry.EG -> R.string.news_region_eg
        NewsCountry.FR -> R.string.news_region_fr
        NewsCountry.DE -> R.string.news_region_de
        NewsCountry.GR -> R.string.news_region_gr
        NewsCountry.HK -> R.string.news_region_hk
        NewsCountry.HU -> R.string.news_region_hu
        NewsCountry.IN -> R.string.news_region_in
        NewsCountry.ID -> R.string.news_region_id
        NewsCountry.IE -> R.string.news_region_ie
        NewsCountry.IL -> R.string.news_region_il
        NewsCountry.IT -> R.string.news_region_it
        NewsCountry.JP -> R.string.news_region_jp
        NewsCountry.LV -> R.string.news_region_lv
        NewsCountry.LT -> R.string.news_region_lt
        NewsCountry.MY -> R.string.news_region_my
        NewsCountry.MX -> R.string.news_region_mx
        NewsCountry.MA -> R.string.news_region_ma
        NewsCountry.NL -> R.string.news_region_nl
        NewsCountry.NZ -> R.string.news_region_nz
        NewsCountry.NG -> R.string.news_region_ng
        NewsCountry.NO -> R.string.news_region_no
        NewsCountry.PH -> R.string.news_region_ph
        NewsCountry.PL -> R.string.news_region_pl
        NewsCountry.PT -> R.string.news_region_pt
        NewsCountry.RO -> R.string.news_region_ro
        NewsCountry.RU -> R.string.news_region_ru
        NewsCountry.SA -> R.string.news_region_sa
        NewsCountry.RS -> R.string.news_region_rs
        NewsCountry.SG -> R.string.news_region_sg
        NewsCountry.SK -> R.string.news_region_sk
        NewsCountry.SI -> R.string.news_region_si
        NewsCountry.ZA -> R.string.news_region_za
        NewsCountry.KR -> R.string.news_region_kr
        NewsCountry.SE -> R.string.news_region_se
        NewsCountry.CH -> R.string.news_region_ch
        NewsCountry.TW -> R.string.news_region_tw
        NewsCountry.TH -> R.string.news_region_th
        NewsCountry.TR -> R.string.news_region_tr
        NewsCountry.AE -> R.string.news_region_ae
        NewsCountry.UA -> R.string.news_region_ua
        NewsCountry.GB -> R.string.news_region_gb
        NewsCountry.US -> R.string.news_region_us
        NewsCountry.VE -> R.string.news_region_ve
    }

@get:StringRes
val NewsLanguage.titleRes: Int
    get() = when (this) {
        NewsLanguage.AR -> R.string.news_language_ar
        NewsLanguage.DE -> R.string.news_language_de
        NewsLanguage.EN -> R.string.news_language_en
        NewsLanguage.ES -> R.string.news_language_es
        NewsLanguage.FR -> R.string.news_language_fr
        NewsLanguage.HE -> R.string.news_language_he
        NewsLanguage.IT -> R.string.news_language_it
        NewsLanguage.NL -> R.string.news_language_nl
        NewsLanguage.NO -> R.string.news_language_no
        NewsLanguage.PT -> R.string.news_language_pt
        NewsLanguage.RU -> R.string.news_language_ru
        NewsLanguage.SV -> R.string.news_language_sv
        NewsLanguage.UD -> R.string.news_language_ud
        NewsLanguage.ZH -> R.string.news_language_zh
    }

@get:StringRes
val ProviderId.titleRes: Int
    get() = when (this) {
        ProviderId.NEWS_API -> R.string.provider_news_api
        ProviderId.NEWS_DATA -> R.string.provider_news_data
        ProviderId.GDELT -> R.string.provider_gdelt
    }

@get:StringRes
val ProviderId.attributionRes: Int
    get() = when (this) {
        ProviderId.NEWS_API -> R.string.provider_news_api
        ProviderId.NEWS_DATA -> R.string.provider_news_data
        ProviderId.GDELT -> R.string.provider_gdelt_attribution
    }

@get:StringRes
val ProviderId.websiteUrlRes: Int
    get() = when (this) {
        ProviderId.NEWS_API -> R.string.provider_news_api_url
        ProviderId.NEWS_DATA -> R.string.provider_news_data_url
        ProviderId.GDELT -> R.string.provider_gdelt_url
    }
