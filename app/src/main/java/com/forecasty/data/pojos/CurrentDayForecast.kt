package com.forecasty.data.pojos

import androidx.room.*
import com.forecasty.domain.local.Converters
import com.forecasty.domain.local.DbConfig.Constants.TABLE_NAME
import com.google.gson.annotations.SerializedName

@Entity(tableName = TABLE_NAME)
data class CurrentDayForecast(
    @SerializedName("id")
    @PrimaryKey
    val id: Int,

    @SerializedName("clouds")
    @Embedded
    val cloudsPercentage: Cloudiness? = null,

    @SerializedName("coord")
    @Embedded
    val coordinates: Coordinates? = null,

    @SerializedName("dt")
    val calculatedWeatherTimeStamp: Long? = null,

    @SerializedName("main")
    @Embedded
    val temp: Temperature? = null,

    @SerializedName("name")
    val locationName: String? = null,

    @SerializedName("sys")
    @Embedded
    val countryInfo: CountryInfo? = null,

    @SerializedName("weather")
    @field:TypeConverters(Converters::class)
    val weather: List<Weather>? = null,

    @SerializedName("wind")
    @Embedded
    val wind: Wind? = null
)