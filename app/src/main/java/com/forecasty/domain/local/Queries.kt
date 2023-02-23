package com.forecasty.domain.local

object Queries {

    const val SELECT_ALL_CURRENT_WEATHER_FROM_TABLE =
        "SELECT * FROM ${DbConfig.Constants.CURRENT_WEATHER_TABLE_NAME}"

    const val SELECT_ALL_FORECASTS_FROM_TABLE =
        "SELECT * FROM ${DbConfig.Constants.FORECASTS_TABLE_NAME}"
}