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
        queryType: QueryType,
        isUnitChanged: Boolean = false
    ): CurrentDayForecast?

    suspend fun getForecast(
        query: Map<String, String>,
        queryType: QueryType
    ): ExtendedForecast?

    suspend fun getCurrentWeatherLastSearchesList(limit: Int): List<CurrentDayForecast>?

    suspend fun getForecastsLastSearchesList(limit: Int): List<ExtendedForecast>?
}