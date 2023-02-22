package com.forecasty.domain.local

import androidx.room.TypeConverter
import com.forecasty.data.pojos.Weather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromWeatherList(weather: List<Weather>?): String? {
        val gson = Gson()
        val type = object : TypeToken<Weather>() {}.type
        return gson.toJson(weather?.firstOrNull(), type)
    }

    @TypeConverter
    fun toWeatherList(json: String?): List<Weather>? {
        if (json != null && json.isEmpty()) return null

        val gson = Gson()
        val type = object : TypeToken<Weather>() {}.type
        return listOf(gson.fromJson(json, type))
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDateTime?): String? {
        val gson = Gson()
        return gson.toJson(date, LocalDateTime::class.java)
    }

    @TypeConverter
    fun toLocalDate(json: String?): LocalDateTime? {
        if (json != null && json.isEmpty()) return null

        val gson = Gson()
        return gson.fromJson(json, LocalDateTime::class.java)
    }
}