package com.forecasty.util

enum class MeasurementUnit(
    val value: String,
    val otherValue: String,
    val temp: String,
    val velocity: String
) {
    METRIC("metric", "Imperial","°C", "m/sec"),
    IMPERIAL("imperial", "Metric", "°F", "miles/hr")
}