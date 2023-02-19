package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class Forecast(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("clouds")
    val cloudsPercentage: Cloudiness? = null,
    @SerializedName("cord")
    val coordinates: Coordinates? = null,
    @SerializedName("dt")
    val calculatedWeatherTimeStamp: Long? = null,
    @SerializedName("main")
    val temp: Temperature? = null,
    @SerializedName("name")
    val locationName: String? = null,
    @SerializedName("sys")
    val countryInfo: CountryInfo? = null,
    @SerializedName("weather")
    val weather: List<Weather>? = null,
    @SerializedName("wind")
    val wind: Wind? = null
)