package com.forecasty.util

import com.forecasty.data.pojos.Coordinates
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

object GsonUtils {

    fun coordinatesToJson(coordinates: Coordinates): String =
        Gson().toJson(coordinates)

    fun getCoordinatesFromString(locationJson: String): Coordinates? =
        try {
            Gson().fromJson(locationJson, Coordinates::class.java)
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            null
        }

    fun unitToJson(unit: MeasurementUnit): String =
        Gson().toJson(unit)

    fun getUnitFromString(unitJson: String): MeasurementUnit? =
        try {
            Gson().fromJson(unitJson, MeasurementUnit::class.java)
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            null
        }

    fun query(query: Pair<Map<String, String>, QueryType>): String =
        Gson().toJson(query)

    fun getQueryFromString(queryJson: String): Pair<Map<String, String>, QueryType>? =
        try {
            val type = object : TypeToken<Pair<Map<String, String>, QueryType>>() {}.type
            Gson().fromJson(queryJson, type)
        } catch (e: Exception) {
            Timber.e(e.localizedMessage)
            null
        }
}