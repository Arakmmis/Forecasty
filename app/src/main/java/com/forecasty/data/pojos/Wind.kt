package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class Wind(
    @SerializedName("deg")
    val degree: Int? = null,
    @SerializedName("speed")
    val speed: Double? = null,
    @SerializedName("gust")
    val gust: Double? = null
)