package com.forecasty.domain.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.forecasty.data.pojos.CurrentDayForecast
import com.forecasty.data.pojos.ExtendedForecast

@Database(
    entities = [CurrentDayForecast::class, ExtendedForecast::class],
    version = DbConfig.Constants.DB_VERSION,
    exportSchema = false
)
abstract class ForecastDb : RoomDatabase() {

    abstract fun forecastDao(): ForecastDao
}