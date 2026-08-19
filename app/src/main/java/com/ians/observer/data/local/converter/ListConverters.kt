package com.ians.observer.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverters {
    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromSetToString(set: Set<String>?): String? {
        return set?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun fromStringToSet(value: String?): Set<String>? {
        if (value == null) return null
        val type = object : TypeToken<Set<String>>() {}.type
        return Gson().fromJson(value, type)
    }
}
