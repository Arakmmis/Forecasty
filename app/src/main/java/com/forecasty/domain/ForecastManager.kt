package com.forecasty.domain

import androidx.lifecycle.LiveData
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.util.QueryType
import com.forecasty.util.SingleLiveEvent

interface ForecastManager {

    var state: SingleLiveEvent<QueryState>

    fun getQueryState(): LiveData<QueryState>

    fun refresh()

    suspend fun getCurrentWeather(
        query: Map<String, String>,
        queryType: QueryType
    ): CurrentDayForecast?

    suspend fun getForecast(
        query: Map<String, String>
    ): ExtendedForecast?

    suspend fun addForecast(forecast: CurrentDayForecast)

    suspend fun removeForecast(forecast: CurrentDayForecast)

    enum class FilterTag {
        TWENTY_FOUR_HOURS, FORTY_EIGHT_HOURS
    }
}