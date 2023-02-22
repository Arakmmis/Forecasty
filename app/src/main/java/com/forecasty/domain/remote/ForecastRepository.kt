package com.forecasty.domain.remote

import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
import com.forecasty.domain.remote.QueryHelper.Keys.LAT
import com.forecasty.domain.remote.QueryHelper.Keys.LON
import com.forecasty.domain.remote.QueryHelper.Keys.ZIP_CODE
import java.time.LocalDateTime
import javax.inject.Inject

class ForecastRepository @Inject constructor(private val api: Api) {

    suspend fun getCurrentWeather(query: Map<String, String>): CurrentDayForecast? =
        api.getCurrentWeather(query).body()?.apply {
            searchTermUsed = when {
                query[CITY_NAME] != null -> query[CITY_NAME]

                query[LAT] != null && query[LON] != null ->
                    String.format("%.7f, %.7f", query[LAT]?.toDouble(), query[LON]?.toDouble())

                query[ZIP_CODE] != null -> query[ZIP_CODE]

                else -> null
            }

            receivedWeatherDateTime = LocalDateTime.now()
        }

    suspend fun getForecast(query: Map<String, String>): ExtendedForecast? =
        api.getForecast(query).body()
}