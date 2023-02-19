package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("icon")
    val iconId: String? = null,
    @SerializedName("main")
    val title: String? = null
)