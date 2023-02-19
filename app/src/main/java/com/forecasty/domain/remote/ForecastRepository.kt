package com.forecasty.domain.remote

import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import javax.inject.Inject

class ForecastRepository @Inject constructor(private val api: Api) {

    suspend fun getCurrentWeather(query: Map<String, String>): CurrentDayForecast? =
        api.getCurrentWeather(query).body()

    suspend fun getForecast(query: Map<String, String>): ExtendedForecast? =
        api.getForecast(query).body()
}