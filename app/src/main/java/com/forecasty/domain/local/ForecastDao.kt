package com.forecasty.domain.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.domain.local.Queries.SELECT_ALL_FROM_TABLE

@Dao
interface ForecastDao {

    @Insert
    suspend fun insertForecast(forecast: CurrentDayForecast)

    @Delete
    suspend fun removeForecast(forecast: CurrentDayForecast)

    @Query("$SELECT_ALL_FROM_TABLE WHERE locationName = :cityName LIMIT 1")
    suspend fun getCurrentWeather(cityName: String): CurrentDayForecast?

    @Query("$SELECT_ALL_FROM_TABLE WHERE lat = :lat AND lon = :lon LIMIT 1")
    suspend fun getCurrentWeather(lat: Double, lon: Double): CurrentDayForecast?

    @Query(SELECT_ALL_FROM_TABLE)
    suspend fun getAllWeather(): List<CurrentDayForecast>
}