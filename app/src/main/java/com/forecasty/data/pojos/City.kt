package com.forecasty.data.pojos

data class City(
    val id: Int,
    val coordinates: Coordinates,
    val country: String,
    val name: String,
    val population: Int,
    val sunrise: Int,
    val sunset: Int,
    val timezone: Int
)