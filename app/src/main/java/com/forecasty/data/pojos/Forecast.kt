package com.forecasty.data.pojos

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.forecasty.domain.local.Converters
import com.google.gson.annotations.SerializedName

data class Forecast(
    @SerializedName("id")
    @PrimaryKey
    val id: Int,

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
    val wind: Wind? = null,

    @SerializedName("dt_txt")
    val forecastDateTime: String? = null
)