package com.forecasty.domain.remote

import com.forecasty.domain.remote.QueryHelper.Keys.CITY_NAME
import com.forecasty.domain.remote.QueryHelper.Keys.LAT
import com.forecasty.domain.remote.QueryHelper.Keys.LON
import com.forecasty.domain.remote.QueryHelper.Keys.MEASUREMENT_UNIT
import com.forecasty.domain.remote.QueryHelper.Keys.ZIP_CODE
import com.forecasty.util.MeasurementUnit

object QueryHelper {

    object Keys {
        const val CITY_NAME = "q"
        const val LAT = "lat"
        const val LON = "lon"
        const val ZIP_CODE = "zip"
        const val MEASUREMENT_UNIT = "units"
    }

    fun byCityName(
        name: String,
        unit: MeasurementUnit = MeasurementUnit.METRIC
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[CITY_NAME] = name

        addMeasurementUnit(map, unit)

        return map
    }

    fun byLatLon(
        lat: Double,
        lon: Double,
        unit: MeasurementUnit = MeasurementUnit.METRIC
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[LAT] = lat.toString()
        map[LON] = lon.toString()

        addMeasurementUnit(map, unit)

        return map
    }

    fun byZipCode(
        zipCode: String,
        unit: MeasurementUnit = MeasurementUnit.METRIC
    ): HashMap<String, String> {
        val map = HashMap<String, String>()
        map[ZIP_CODE] = zipCode

        addMeasurementUnit(map, unit)

        return map
    }

    private fun addMeasurementUnit(
        query: HashMap<String, String>,
        unit: MeasurementUnit
    ): HashMap<String, String> {
        query[MEASUREMENT_UNIT] = unit.value
        return query
    }
}