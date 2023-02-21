package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lon")
    val lon: Double? = null
) {

    fun validateLatitude(): Boolean {
        return lat != null && lat != 0.0
    }

    fun validateLongitude(): Boolean {
        return lon != null && lon != 0.0
    }
}