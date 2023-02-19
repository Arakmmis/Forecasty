package com.forecasty.domain

import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast

interface ForecastManager {

    suspend fun getCurrentWeather(
        query: Map<String, String>
    ): CurrentDayForecast

    suspend fun getForecast(
        query: Map<String, String>
    ): ExtendedForecast
}