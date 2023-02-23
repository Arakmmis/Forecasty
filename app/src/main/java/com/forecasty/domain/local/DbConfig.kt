package com.forecasty.domain.local

object DbConfig {

    object Constants {
        const val DB_NAME = "Forecast.db"
        const val CURRENT_WEATHER_TABLE_NAME = "current_weather"
        const val FORECASTS_TABLE_NAME = "forecasts"
        const val DB_VERSION = 1
        const val CURRENT_WEATHER_TABLE_SIZE_LIMIT = 10
        const val CURRENT_WEATHER_LAST_SEARCHED_LIMIT = 10
        const val FORECASTS_TABLE_SIZE_LIMIT = 5
        const val FORECASTS_LAST_SEARCHED_LIMIT = 5
    }
}