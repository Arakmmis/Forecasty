package com.forecasty.domain.remote

import com.forecasty.domain.ForecastManager.FilterTag
import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
import com.forecasty.domain.remote.QueryHelper.Keys.LAT
import com.forecasty.domain.remote.QueryHelper.Keys.LON
import com.forecasty.domain.remote.QueryHelper.Keys.TIMESTAMPS_COUNT
import com.forecasty.domain.remote.QueryHelper.Keys.ZIP_CODE
import com.forecasty.domain.remote.QueryHelper.Values.FORTY_EIGHT_HOURS_TIMESTAMPS
import com.forecasty.domain.remote.QueryHelper.Values.TWENTY_FOUR_HOURS_TIMESTAMPS

object QueryHelper {

    object Keys {
        const val CITY_NAME = "q"
        const val LAT = "lat"
        const val LON = "lon"
        const val ZIP_CODE = "zip"
        const val TIMESTAMPS_COUNT = "cnt"
    }

    object Values {
        const val TWENTY_FOUR_HOURS_TIMESTAMPS = "8"
        const val FORTY_EIGHT_HOURS_TIMESTAMPS = "16"
    }

    fun byCityName(
        name: String,
        filterTag: FilterTag? = null
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[CITY_NAME] = name

        when (filterTag) {
            FilterTag.TWENTY_FOUR_HOURS ->
                map[TIMESTAMPS_COUNT] = TWENTY_FOUR_HOURS_TIMESTAMPS

            FilterTag.FORTY_EIGHT_HOURS ->
                map[TIMESTAMPS_COUNT] = FORTY_EIGHT_HOURS_TIMESTAMPS

            else -> {}
        }

        return map
    }

    fun byLatLon(
        lat: Long,
        lon: Long,
        filterTag: FilterTag? = null
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[LAT] = lat.toString()
        map[LON] = lon.toString()

        when (filterTag) {
            FilterTag.TWENTY_FOUR_HOURS ->
                map[TIMESTAMPS_COUNT] = TWENTY_FOUR_HOURS_TIMESTAMPS

            FilterTag.FORTY_EIGHT_HOURS ->
                map[TIMESTAMPS_COUNT] = FORTY_EIGHT_HOURS_TIMESTAMPS

            else -> {}
        }

        return map
    }

    fun byZipCode(
        zipCode: String,
        filterTag: FilterTag? = null
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[ZIP_CODE] = zipCode

        when (filterTag) {
            FilterTag.TWENTY_FOUR_HOURS ->
                map[TIMESTAMPS_COUNT] = TWENTY_FOUR_HOURS_TIMESTAMPS

            FilterTag.FORTY_EIGHT_HOURS ->
                map[TIMESTAMPS_COUNT] = FORTY_EIGHT_HOURS_TIMESTAMPS

            else -> {}
        }

        return map
    }
}