package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class ExtendedForecast(
    @SerializedName("city")
    val city: City? = null,
    @SerializedName("cnt")
    val timeStampsReturned: Int? = null,
    @SerializedName("list")
    val forecasts: List<Forecast>? = null
)