package com.forecasty.domain.remote

import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface Api {

    @GET(URLs.URL_GET_CURRENT_WEATHER)
    suspend fun getCurrentWeather(
        @QueryMap params: Map<String, String>
    ): Response<CurrentDayForecast>

    @GET(URLs.URL_FORECAST)
    suspend fun getForecast(
        @QueryMap params: Map<String, String>
    ): Response<ExtendedForecast>
}