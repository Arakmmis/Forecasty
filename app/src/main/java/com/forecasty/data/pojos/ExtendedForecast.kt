package com.forecasty.data.pojos

import androidx.room.*
import com.forecasty.domain.local.Converters
import com.forecasty.domain.local.DbConfig
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

@Entity(tableName = DbConfig.Constants.FORECASTS_TABLE_NAME)
data class ExtendedForecast(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @SerializedName("city")
    @Embedded(prefix = "city_")
    val city: City? = null,

    @SerializedName("cnt")
    val timeStampsReturned: Int? = null,

    @SerializedName("list")
    @field:TypeConverters(Converters::class)
    val forecasts: List<Forecast>? = null,

    @field:TypeConverters(Converters::class)
    var receivedWeatherDateTime: LocalDateTime? = null,

    var searchTermUsed: String? = null
)