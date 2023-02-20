package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast

interface ForecastManager {

    fun getQueryState(): LiveData<QueryState>

    fun refresh()

    fun retryFailedQuery()

    suspend fun getCurrentWeather(
        query: Map<String, String>,
        queryTag: QueryTag
    ): CurrentDayForecast?

    suspend fun getForecast(
        query: Map<String, String>
    ): ExtendedForecast?

    suspend fun addForecast(forecast: CurrentDayForecast)

    suspend fun removeForecast(forecast: CurrentDayForecast)

    enum class QueryTag {
        CITY_NAME, LAT_LON, ZIP_CODE
    }

    enum class FilterTag {
        TWENTY_FOUR_HOURS, FORTY_EIGHT_HOURS
    }
}