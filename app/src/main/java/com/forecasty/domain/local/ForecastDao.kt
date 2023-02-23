package com.forecasty.domain.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast
import com.forecasty.domain.local.Queries.SELECT_ALL_CURRENT_WEATHER_FROM_TABLE
import com.forecasty.domain.local.Queries.SELECT_ALL_FORECASTS_FROM_TABLE

@Dao
interface ForecastDao {

    /**
     * CURRENT WEATHER
     */
    @Insert
    suspend fun insertCurrentWeather(forecast: CurrentDayForecast)

    @Delete
    suspend fun removeCurrentWeather(forecast: CurrentDayForecast)

    @Query("$SELECT_ALL_CURRENT_WEATHER_FROM_TABLE WHERE locationName = :cityName LIMIT 1")
    suspend fun getCurrentWeather(cityName: String): CurrentDayForecast?

    @Query("$SELECT_ALL_CURRENT_WEATHER_FROM_TABLE WHERE lat = :lat AND lon = :lon = :lon LIMIT 1")
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentDayForecast?

    @Query(SELECT_ALL_CURRENT_WEATHER_FROM_TABLE)
    suspend fun getAllWeather(): List<CurrentDayForecast>

    /**
     * FORECASTS
     */
    @Insert
    suspend fun insertForecast(forecast: ExtendedForecast)

    @Delete
    suspend fun removeForecast(forecast: ExtendedForecast)

    @Query("$SELECT_ALL_FORECASTS_FROM_TABLE WHERE city_name = :cityName LIMIT 1")
    suspend fun getForecast(cityName: String): ExtendedForecast?

    @Query("$SELECT_ALL_FORECASTS_FROM_TABLE WHERE city_lat = :lat AND city_lon = :lon LIMIT 1")
    suspend fun getForecast(lat: Double, lon: Double): ExtendedForecast?

    @Query(SELECT_ALL_FORECASTS_FROM_TABLE)
    suspend fun getAllForecasts(): List<ExtendedForecast>
}