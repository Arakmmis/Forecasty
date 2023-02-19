package com.forecasty.data.helpers

object QueryHelper {

    fun byCityName(name: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        map["q"] = name
        return map
    }

    fun byLatLon(lat: Long, lon: Long): HashMap<String, String> {
        val map = HashMap<String, String>()
        map["lat"] = lat.toString()
        map["lon"] = lon.toString()
        return map
    }

    fun byZipCode(zipCode: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        map["zip"] = zipCode
        return map
    }
}