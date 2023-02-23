package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class CountryInfo(
    @SerializedName("country")
    val name: String? = null
)
