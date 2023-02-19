package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class Cloudiness(
    @SerializedName("all")
    val percentage: Int? = null
)