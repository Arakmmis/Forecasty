package com.forecasty.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

// Example Result: Dec 31
fun String.getFormattedDate(): String {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatter = DateTimeFormatter.ofPattern("MMM dd")
    return LocalDate.parse(this, pattern).format(formatter)
}

// Example Result: Sunday @ 18:30
fun String.getDayOfWeekAndTimeFromDate(): String {
    return String.format("%s @ %s", this.getDayOfWeekFromDate(), this.getTimeFromDate())
}

private fun String.getDayOfWeekFromDate(): String {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDate.parse(this, pattern).dayOfWeek.name.lowercase()
        .replaceFirstChar {
            if (it.isLowerCase())
                it.titlecase(Locale.getDefault())
            else
                it.toString()
        }
}

private fun String.getTimeFromDate(): String {
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return LocalTime.parse(this, pattern).format(formatter)
}