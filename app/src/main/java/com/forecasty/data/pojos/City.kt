package com.forecasty.data.pojos

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id")
    val id: Int,

    @SerializedName("coord")
    @Embedded
    val coordinates: Coordinates? = null,

    @SerializedName("country")
    val country: String? = null,

    @SerializedName("name")
    val name: String? = null
)