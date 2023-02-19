package com.forecasty.domain

import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.remote.ForecastRepository
import javax.inject.Inject

class ForecastManagerImpl @Inject constructor(
    repo: ForecastRepository
): ForecastManager {

    override suspend fun getCurrentWeather(query: Map<String, String>): CurrentDayForecast {
        TODO("Not yet implemented")
    }

    override suspend fun getForecast(query: Map<String, String>): ExtendedForecast {
        TODO("Not yet implemented")
    }
}