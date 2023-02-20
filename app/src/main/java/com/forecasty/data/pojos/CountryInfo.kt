package com.forecasty.data.pojos

import com.google.gson.annotations.SerializedName

data class CountryInfo(
    @SerializedName("country")
    val name: String? = null,
    @SerializedName("sunrise")
    val sunriseTimeStamp: Long? = null,
    @SerializedName("sunset")
    val sunsetTimeStamp: Long? = null,
    // Either n - night or d - day
    @SerializedName("pod")
    val partOfDay: String? = null
) {
    fun getPoD(): PartOfDay =
        when (partOfDay) {
            "d" -> PartOfDay.DAY
            "n" -> PartOfDay.NIGHT
            else -> throw IllegalStateException("partOfDay value is undefined")
        }

    enum class PartOfDay {
        DAY, NIGHT
    }
}
